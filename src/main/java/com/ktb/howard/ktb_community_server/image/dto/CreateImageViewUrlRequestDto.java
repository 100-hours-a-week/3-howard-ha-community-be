package com.ktb.howard.ktb_community_server.image.dto;

import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class CreateImageViewUrlRequestDto {

    private ImageType imageType;

    private List<ImageViewRequestInfoDto> images;

    public record ImageViewRequestInfoDto(
            ImageType imageType,
            Long imageId,
            Integer sequence
    ) { }

}
