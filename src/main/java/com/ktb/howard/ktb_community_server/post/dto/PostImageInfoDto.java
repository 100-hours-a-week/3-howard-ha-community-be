package com.ktb.howard.ktb_community_server.post.dto;

import java.time.Instant;

public record PostImageInfoDto(String postImageUrl, Integer sequence, Instant expiresAt) { }
