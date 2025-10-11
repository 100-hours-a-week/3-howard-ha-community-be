package com.ktb.howard.ktb_community_server.comment.dto;

public record CreateCommentResponseDto(
        Long commentId,
        Long postId,
        Integer memberId,
        Long parentCommentId,
        String content
) { }
