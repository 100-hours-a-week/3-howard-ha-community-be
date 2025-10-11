package com.ktb.howard.ktb_community_server.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCommentRequestDto(
        @Positive
        @NotNull(message = "postId는 필수값입니다.")
        Long postId,
        @Positive
        @NotNull(message = "memberId는 필수값입니다.")
        Integer memberId,
        @Positive
        Long parentCommentId,
        @NotBlank(message = "content는 필수값입니다.")
        String content
) { }
