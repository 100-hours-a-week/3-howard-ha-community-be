package com.ktb.howard.ktb_community_server.image.dto;

import org.springframework.http.HttpMethod;

import java.time.Instant;

public record ImageUrlResponseDto(
        String url,
        Long imageId,
        Integer sequence,
        HttpMethod httpMethod,
        Instant expiresAt
) { }
