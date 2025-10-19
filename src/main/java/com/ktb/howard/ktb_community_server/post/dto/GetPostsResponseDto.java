package com.ktb.howard.ktb_community_server.post.dto;

import com.ktb.howard.ktb_community_server.member.dto.MemberInfoResponseDto;

import java.time.LocalDateTime;

public record GetPostsResponseDto(
        Long postId,
        String title,
        Integer likeCount,
        Long commentCount,
        Long viewCount,
        LocalDateTime createdAt,
        MemberInfoResponseDto writer
) { }
