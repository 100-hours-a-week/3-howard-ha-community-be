package com.ktb.howard.ktb_community_server.post.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record GetPostsDto(
        Long postId,
        String title,
        Integer likeCount,
        Long viewCount,
        Long commentCount,
        Integer memberId,
        String email,
        String nickname,
        Long imageId,
        String objectKey,
        Integer sequence,
        LocalDateTime createdAt
) {
    @QueryProjection
    public GetPostsDto { }
}