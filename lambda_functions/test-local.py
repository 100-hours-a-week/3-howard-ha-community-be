import base64
import json
import os
from upload_image import lambda_handler

def create_multipart_payload(file_path, image_type='PROFILE', sequence=1, boundary="simpleboundary"):
    """
    Multipart/form-data 요청의 raw body를 시뮬레이션하는 함수
    """
    crlf = b"\r\n"
    body = b""

    # 1. 파일 파트 추가 (key='file')
    with open(file_path, 'rb') as f:
        image_bytes = f.read()

    filename = os.path.basename(file_path)
    mime_type = "image/jpeg" if filename.lower().endswith(('.jpg', '.jpeg')) else "image/png"

    body += f'--{boundary}'.encode() + crlf
    body += f'Content-Disposition: form-data; name="file"; filename="{filename}"'.encode() + crlf
    body += f'Content-Type: {mime_type}'.encode() + crlf
    body += crlf
    body += image_bytes + crlf

    # 2. 텍스트 파트 추가 (key='type')
    body += f'--{boundary}'.encode() + crlf
    body += f'Content-Disposition: form-data; name="type"'.encode() + crlf
    body += crlf
    body += image_type.encode() + crlf

    # 3. 텍스트 파트 추가 (key='sequence')
    body += f'--{boundary}'.encode() + crlf
    body += f'Content-Disposition: form-data; name="sequence"'.encode() + crlf
    body += crlf
    body += str(sequence).encode() + crlf

    # 4. 종료 파트
    body += f'--{boundary}--'.encode() + crlf

    return {
        "body": base64.b64encode(body).decode('utf-8'),
        "isBase64Encoded": True,
        "headers": {
            "Content-Type": f"multipart/form-data; boundary={boundary}"
        }
    }


# --- 메인 테스트 실행 ---

TEST_IMAGE_PATH = 'test_image.jpeg'

if not os.path.exists(TEST_IMAGE_PATH):
    print(f"ERROR: 테스트 이미지 파일 '{TEST_IMAGE_PATH}'이(가) 현재 디렉토리에 없습니다.")
else:
    # 1. 테스트 페이로드 생성
    test_payload = create_multipart_payload(
        file_path=TEST_IMAGE_PATH,
        image_type='POST',
        sequence=5
    )

    # 2. Lambda Event 객체 구성
    test_event = {
        'httpMethod': 'POST',
        'body': test_payload['body'],
        'isBase64Encoded': test_payload['isBase64Encoded'],
        'headers': test_payload['headers']
    }

    # 3. 함수 호출
    print("--- Lambda Handler 호출 결과 ---")
    response = lambda_handler(test_event, None)

    # 4. 결과 출력
    # 응답 body는 문자열이므로 다시 JSON으로 로드하여 출력
    response_body = json.loads(response['body'])

    print(json.dumps(response_body, indent=2, ensure_ascii=False))