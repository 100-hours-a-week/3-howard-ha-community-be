package com.ktb.howard.ktb_community_server.post.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreatePostRequestDto(
        @NotBlank(message = "제목은 필수값 입니다.")
        String title,
        @NotBlank(message = "본문은 필수값 입니다.")
        String content,
        List<PostImageRequestInfoDto> postImages
) { }
