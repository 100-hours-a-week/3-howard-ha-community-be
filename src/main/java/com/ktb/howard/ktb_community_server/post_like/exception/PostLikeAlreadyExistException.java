package com.ktb.howard.ktb_community_server.post_like.exception;

public class PostLikeAlreadyExistException extends RuntimeException {
    public PostLikeAlreadyExistException(String message) {
        super(message);
    }
}
