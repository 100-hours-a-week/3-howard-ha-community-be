package com.ktb.howard.ktb_community_server.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    // 1. 응답 성공/실패 여부 (불변의 응답 상태)
    private final Boolean isSuccess;

    // 2. 비즈니스 처리 결과 코드 (커스텀 코드)
    private final String code;

    // 3. 응답 메시지 (성공 또는 에러 메시지)
    private final String message;

    // 4. 실제 응답 데이터. 데이터가 없을 경우(예: 성공/실패 메시지만 전달) JSON에서 제외
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T payload;

    // 4-1. 데이터가 포함된 성공 응답
    public static <T> ApiResponse<T> onSuccess(T data) {
        return ApiResponse.<T>builder()
                .isSuccess(true)
                .code("COMMON_2000") // 예시: 성공 기본 코드
                .message("요청에 성공하였습니다.")
                .payload(data)
                .build();
    }

    // 4-2. 데이터 없이 메시지만 전달하는 성공 응답 (예: 삭제 성공)
    public static <T> ApiResponse<T> onSuccess() {
        return ApiResponse.<T>builder()
                .isSuccess(true)
                .code("COMMON_2000")
                .message("요청에 성공하였습니다.")
                .payload(null)
                .build();
    }

    // 5. 비즈니스 에러 발생 시 사용하는 실패 응답
    public static <T> ApiResponse<T> onFailure(BaseErrorCode code) {
        return ApiResponse.<T>builder()
                .isSuccess(false)
                .code(code.getReason().getCode())
                .message(code.getReason().getMessage())
                .payload(null)
                .build();
    }
}
