package com.ktb.howard.ktb_community_server.post.dto;

import java.util.List;

public record CreatePostResponseDto(Long postId, Integer writerId, String title, String content, List<PostImageRequestInfoDto> postImages) { }
