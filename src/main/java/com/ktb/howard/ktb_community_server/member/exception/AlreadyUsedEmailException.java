package com.ktb.howard.ktb_community_server.member.exception;

import lombok.Getter;

@Getter
public class AlreadyUsedEmailException extends RuntimeException {

    private final String fieldName = "email";
    private final String value;

    public AlreadyUsedEmailException(String message, String value) {
        super(message);
        this.value = value;
    }

}
