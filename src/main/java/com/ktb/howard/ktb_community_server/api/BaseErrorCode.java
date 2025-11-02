package com.ktb.howard.ktb_community_server.api;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {

    String getCode();

    String getMessage();

    HttpStatus getHttpStatus();

}
