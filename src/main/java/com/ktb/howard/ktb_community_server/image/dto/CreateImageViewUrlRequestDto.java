package com.ktb.howard.ktb_community_server.image.dto;

import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateImageViewUrlRequestDto {

    private ImageType imageType;

    private Long referenceId;

}
