package com.ktb.howard.ktb_community_server.exception;

import java.util.List;

public record ValidationErrorResponse(String message, List<ValidationError> errors) {
    public record ValidationError(
            String field,
            String message
    ) { }
}
