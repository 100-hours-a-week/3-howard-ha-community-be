package com.ktb.howard.ktb_community_server.image.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class GetImageUploadUrlResponseDto {

    private List<ImageUploadResponseInfoDto> images;

    public record ImageUploadResponseInfoDto(
            String url,
            Long reservedId,
            LocalDateTime expiresAt
    ) { }

}
