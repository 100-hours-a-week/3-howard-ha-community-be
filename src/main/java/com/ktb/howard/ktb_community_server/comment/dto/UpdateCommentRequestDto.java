package com.ktb.howard.ktb_community_server.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequestDto(
        @NotBlank(message = "댓글 본문은 필수로 입력되어야 합니다.")
        String content
) { }
