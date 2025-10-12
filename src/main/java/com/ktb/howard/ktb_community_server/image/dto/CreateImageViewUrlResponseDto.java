package com.ktb.howard.ktb_community_server.image.dto;

import com.ktb.howard.ktb_community_server.image.domain.ImageType;

import java.time.LocalDateTime;
import java.util.List;

public class CreateImageViewUrlResponseDto {



    private List<CreatedImageViewUrlInfoDto> images;

    public record CreatedImageViewUrlInfoDto(
            ImageType imageType,
            String imageUrl,
            Integer sequence,
            LocalDateTime expiresAt
    ) { }

}
