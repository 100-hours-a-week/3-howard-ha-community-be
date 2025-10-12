package com.ktb.howard.ktb_community_server.post.dto;

import java.time.LocalDateTime;

public record PostImageInfoDto(String postImageUrl, Integer sequence, LocalDateTime expiresAt) { }
