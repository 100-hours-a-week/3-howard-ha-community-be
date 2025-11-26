package com.ktb.howard.ktb_community_server.image.dto;

import java.time.LocalDateTime;

public record CreateImageResponseDto(
        Long imageId,
        String fileName,
        LocalDateTime createdAt
) { }
