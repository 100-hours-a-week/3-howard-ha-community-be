package com.ktb.howard.ktb_community_server.infra.aws.lambda.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktb.howard.ktb_community_server.infra.aws.lambda.dto.LambdaRequestDto;
import com.ktb.howard.ktb_community_server.infra.aws.lambda.exception.LambdaFunctionCallFailedException;
import com.ktb.howard.ktb_community_server.infra.aws.lambda.exception.LambdaResultSerializeFailedException;
import com.ktb.howard.ktb_community_server.infra.aws.s3.dto.PresignedUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LogType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.ktb.howard.ktb_community_server.api.LambdaErrorCode.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class LambdaService {

    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;
    private static final String FUNCTION_NAME = "ktb-howard-leum-s3-lambda";

    public PresignedUrl getUploadPresignedUrl(LambdaRequestDto requestDto) {
        try {
            // 1. 요청 DTO를 JSON 문자열로 변환 후 다시 ByteBuffer로 변환
            String jsonPayload = objectMapper.writeValueAsString(requestDto);
            SdkBytes payload = SdkBytes.fromUtf8String(jsonPayload);
            // 2. InvokeRequest 생성
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(FUNCTION_NAME)
                    .payload(payload)
                    // 동기식 호출 (Sync) 사용
                    .logType(LogType.TAIL) // CloudWatch 로그를 응답에 포함시킬지 여부 설정
                    .build();
            // 3. Lambda 함수 호출 및 응답 수신
            InvokeResponse response = lambdaClient.invoke(invokeRequest);
            // 4. 오류 처리 (Lambda 함수 내부에서 예외가 발생한 경우)
            if (response.functionError() != null) {
                String errorDetails = response.payload().asString(StandardCharsets.UTF_8);
                log.error("Lambda 함수 내부 오류 발생: Resource={}, FunctionError={}", FUNCTION_NAME, response.functionError());
                log.error("Lambda Error Payload Details: {}", errorDetails);
                throw new LambdaFunctionCallFailedException(FUNCTION_CALL_FAILED);
            }
            // 5. 응답 JSON을 PresignedUrl DTO로 역직렬화
            String responseJson = response.payload().asString(StandardCharsets.UTF_8);
            return objectMapper.readValue(responseJson, PresignedUrl.class);
        } catch (IOException e) {
            throw new LambdaResultSerializeFailedException(RESULT_SERIALIZE_ERROR);
        } catch (Exception e) {
            log.error("lambda 함수 실행 실패 = {}", e.getMessage(), e);
            throw new LambdaFunctionCallFailedException(FUNCTION_CALL_FAILED);
        }
    }

}
