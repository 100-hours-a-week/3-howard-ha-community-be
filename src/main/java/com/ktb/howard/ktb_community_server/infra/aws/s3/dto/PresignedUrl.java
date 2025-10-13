package com.ktb.howard.ktb_community_server.infra.aws.s3.dto;

import org.springframework.http.HttpMethod;

import java.time.Instant;

public record PresignedUrl(String presignedUrl, HttpMethod httpMethod, Instant expiresAt) { }
