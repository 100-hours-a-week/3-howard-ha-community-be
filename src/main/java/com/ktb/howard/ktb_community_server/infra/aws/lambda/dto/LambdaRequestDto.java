package com.ktb.howard.ktb_community_server.infra.aws.lambda.dto;

public record LambdaRequestDto(
        String bucket,
        String objectKey,
        String contentType,
        Long contentLength,
        Integer putTtlMin
) { }