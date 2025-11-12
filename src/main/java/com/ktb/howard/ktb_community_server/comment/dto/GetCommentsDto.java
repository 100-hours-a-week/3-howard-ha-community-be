package com.ktb.howard.ktb_community_server.comment.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record GetCommentsDto(
        Long commentId,
        String content,
        Integer memberId,
        String email,
        String nickname,
        Long imageId,
        String objectKey,
        Integer sequence,
        LocalDateTime createdAt,
        LocalDateTime deletedAt
) {
    @QueryProjection
    public GetCommentsDto {}
}
