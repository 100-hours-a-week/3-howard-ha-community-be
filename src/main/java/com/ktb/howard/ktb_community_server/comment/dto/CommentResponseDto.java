package com.ktb.howard.ktb_community_server.comment.dto;

import com.ktb.howard.ktb_community_server.comment.domain.Comment;
import com.ktb.howard.ktb_community_server.member.dto.MemberInfoResponseDto;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {

    private final Long commentId;

    private final String content;

    private final MemberInfoResponseDto writerInfo;

    private final LocalDateTime createdAt;

    public CommentResponseDto(Comment comment, String writerProfileImageUrl) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.writerInfo = new MemberInfoResponseDto(
                comment.getMember().getEmail(),
                comment.getMember().getNickname(),
                writerProfileImageUrl
        );
        this.createdAt = comment.getCreatedAt();
    }

}
