package com.ktb.howard.ktb_community_server.image.dto;

import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class CreateImageUploadUrlRequestDto {

    private ImageType imageType;

    private List<ImageMetadata> imageMetadataList;

}
