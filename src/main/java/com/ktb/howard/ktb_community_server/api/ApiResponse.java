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

    private final Boolean isSuccess;
    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T payload;

    public static <T> ApiResponse<T> onSuccess(T data) {
        return ApiResponse.<T>builder()
                .isSuccess(true)
                .code("SUCCESS")
                .message("요청에 성공하였습니다.")
                .payload(data)
                .build();
    }

    public static <T> ApiResponse<T> onSuccess() {
        return ApiResponse.<T>builder()
                .isSuccess(true)
                .code("SUCCESS")
                .message("요청에 성공하였습니다.")
                .payload(null)
                .build();
    }

    public static <T> ApiResponse<T> onFailure(BaseErrorCode code) {
        return ApiResponse.<T>builder()
                .isSuccess(false)
                .code(code.getCode())
                .message(code.getMessage())
                .payload(null)
                .build();
    }

}
