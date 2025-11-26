package com.ktb.howard.ktb_community_server.image.dto;

import com.ktb.howard.ktb_community_server.image.domain.ImageStatus;
import com.ktb.howard.ktb_community_server.image.domain.ImageType;

public record CreateImageRequestDto(
        ImageType type,
        String fileName,
        Long fileSize,
        String mimeType,
        Integer sequence,
        ImageStatus imageStatus
) { }
