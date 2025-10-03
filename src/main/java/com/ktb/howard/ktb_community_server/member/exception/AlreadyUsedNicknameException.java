package com.ktb.howard.ktb_community_server.member.exception;

import lombok.Getter;

@Getter
public class AlreadyUsedNicknameException extends RuntimeException {

    private final String fieldName = "nickname";
    private final String value;

    public AlreadyUsedNicknameException(String message, String value) {
        super(message);
        this.value = value;
    }

}
