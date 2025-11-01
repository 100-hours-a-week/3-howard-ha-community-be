package com.ktb.howard.ktb_community_server.api;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ReasonDto {

    private HttpStatus httpStatus;

    private final boolean isSuccess;

    private final String code;

    private final String message;

}