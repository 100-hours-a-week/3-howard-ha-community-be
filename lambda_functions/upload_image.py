import json
import base64
import io
import os
import uuid
import logging
import boto3
import requests
from dotenv import load_dotenv
from PIL import Image
from requests_toolbelt.multipart import decoder

load_dotenv()
logger = logging.getLogger()
logger.setLevel(logging.INFO)

S3_BUCKET = os.environ.get('S3_BUCKET')
API_SERVER_URL = os.environ.get('API_SERVER_URL')
INTERNAL_SECRET_KEY = os.environ.get('INTERNAL_SECRET_KEY')
MAX_FILE_SIZE = 1 * 1024 * 1024
ALLOWED_FORMATS = ['JPEG', 'PNG', 'WEBP']

s3_client = boto3.client('s3')


def create_response(status_code, is_success, code, message, payload=None):
    body = {"isSuccess": is_success, "code": code, "message": message, "payload": payload}
    return {
        "statusCode": status_code,
        "headers": {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "OPTIONS, POST",
            "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Amz-Date, X-Api-Key, X-Amz-Security-Token"
        },
        "body": json.dumps(body, ensure_ascii=False)
    }


def on_success(payload=None):
    return create_response(200, True, "SUCCESS", "요청에 성공하였습니다.", payload)


def on_failure(status_code, error_code, message):
    return create_response(status_code, False, error_code, message, None)


def get_multipart_field(parts, field_name):
    for part in parts:
        content_disposition = part.headers.get(b'Content-Disposition', b'').decode()
        if f'name="{field_name}"' in content_disposition:
            return part
    return None


def lambda_handler(event, context):
    try:
        # -------------------------------------------------------------
        # [핵심 수정] HTTP Method 추출 로직 개선 (v1/v2 호환)
        # -------------------------------------------------------------
        http_method = event.get('httpMethod')  # REST API or HTTP API v1
        if not http_method and 'requestContext' in event:
            # HTTP API v2
            http_method = event['requestContext'].get('http', {}).get('method')

        # 로그로 확인 (CloudWatch에서 확인 가능)
        logger.info(f"Received Method: {http_method}")

        # 1. OPTIONS 요청 처리
        if http_method == 'OPTIONS':
            return {
                "statusCode": 200,
                "headers": {
                    "Access-Control-Allow-Origin": "*",
                    "Access-Control-Allow-Methods": "OPTIONS, POST",
                    "Access-Control-Allow-Headers": "Content-Type, Authorization, *",
                    "Access-Control-Max-Age": "86400"
                },
                "body": ""
            }

        # 2. 본문 검사
        if not event.get('body'):
            return on_failure(400, "BAD_REQUEST", "요청 본문이 비어있습니다.")

        # ... (이하 로직은 기존과 동일) ...
        # ... Content-Type 확인, 디코딩, 파싱 등 ...

        headers = event.get('headers', {})
        # 헤더 키 대소문자 문제 방지 (소문자로 변환하여 찾기)
        # HTTP API v2는 헤더를 소문자로 줄 수도 있음
        content_type = None
        for key, value in headers.items():
            if key.lower() == 'content-type':
                content_type = value
                break

        if not content_type or 'multipart/form-data' not in content_type:
            return on_failure(400, "INVALID_CONTENT_TYPE", "Content-Type은 multipart/form-data 여야 합니다.")

        # 2-2. Body 데이터 디코딩
        body_content = event['body']
        if event.get('isBase64Encoded', False):
            body_content = base64.b64decode(body_content)
        else:
            body_content = body_content.encode('utf-8')

        try:
            multipart_data = decoder.MultipartDecoder(body_content, content_type)
        except Exception as e:
            logger.error(f"Multipart Parsing Error: {str(e)}")
            return on_failure(400, "MULTIPART_PARSE_ERROR", "파일 데이터를 파싱할 수 없습니다.")

        part_file = get_multipart_field(multipart_data.parts, 'file')
        part_type = get_multipart_field(multipart_data.parts, 'type')
        part_sequence = get_multipart_field(multipart_data.parts, 'sequence')

        if not part_file:
            return on_failure(400, "MISSING_FILE", "파일(key='file')이 전송되지 않았습니다.")

        file_bytes = part_file.content
        image_type = part_type.content.decode('utf-8') if part_type else 'PROFILE'
        try:
            sequence = int(part_sequence.content.decode('utf-8')) if part_sequence else 1
        except ValueError:
            sequence = 1

        file_size = len(file_bytes)
        if file_size > MAX_FILE_SIZE:
            return on_failure(413, "FILE_TOO_LARGE", "1MB 이하만 가능")

        # 4. 이미지 정밀 검증
        try:
            image = Image.open(io.BytesIO(file_bytes))
            if image.format not in ALLOWED_FORMATS:
                return on_failure(415, "UNSUPPORTED", "지원하지 않는 형식")
            if getattr(image, "is_animated", False):
                return on_failure(400, "ANIMATED_NOT_ALLOWED", "애니메이션 불가")

            extension = image.format.lower()
            if extension == 'jpeg': extension = 'jpg'
            image.verify()
            mime_type = f"image/{extension}"
        except Exception as e:
            return on_failure(400, "INVALID_IMAGE", "유효하지 않은 이미지")

        # 5. S3 업로드
        file_uuid = str(uuid.uuid4())
        s3_filename = f"{file_uuid}.{extension}"
        sub_dir = 'profiles' if image_type == 'PROFILE' else 'posts'
        s3_key = f"tmp/{sub_dir}/{s3_filename}"

        try:
            s3_client.put_object(Bucket=S3_BUCKET, Key=s3_key, Body=file_bytes, ContentType=mime_type)
        except Exception as e:
            logger.error(f"S3 Error: {str(e)}")
            return on_failure(500, "S3_FAIL", "업로드 실패")

        # 6. API 호출
        try:
            api_payload = {
                "type": image_type, "fileName": s3_filename, "fileSize": file_size,
                "mimeType": mime_type, "sequence": sequence, "imageStatus": "TEMPORAL"
            }
            headers_api = {"Content-Type": "application/json", "x-lambda-secret-key": INTERNAL_SECRET_KEY}
            api_res = requests.post(API_SERVER_URL, json=api_payload, headers=headers_api, timeout=5)

            if api_res.status_code != 200: raise Exception("API Error")
            final_payload = api_res.json().get('payload')
        except Exception as e:
            s3_client.delete_object(Bucket=S3_BUCKET, Key=s3_key)
            return on_failure(502, "API_SYNC_FAIL", "메타데이터 저장 실패")

        return on_success(final_payload)

    except Exception as e:
        logger.error(f"System Error: {str(e)}")
        return on_failure(500, "INTERNAL_ERROR", "서버 오류")