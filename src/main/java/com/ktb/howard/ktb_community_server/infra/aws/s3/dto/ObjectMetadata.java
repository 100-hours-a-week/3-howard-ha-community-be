package com.ktb.howard.ktb_community_server.infra.aws.s3.dto;

import java.time.Instant;

public record ObjectMetadata(
        String objectKey,
        Long contentLength,
        String contentType,
        String eTag,
        Instant lastModified
) { }
