package com.ktb.howard.ktb_community_server.image.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.http.HttpMethod;

import java.time.Instant;

public record ImageUrlResponseDto(
        String url,
        Long imageId,
        Integer sequence,
        @JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
        HttpMethod httpMethod,
        Instant expiresAt
) { }
