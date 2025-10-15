package com.ktb.howard.ktb_community_server.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PostUpdateRequestDto {

    private String title;

    private String content;

    private List<PostImageRequestInfoDto> images;

}
