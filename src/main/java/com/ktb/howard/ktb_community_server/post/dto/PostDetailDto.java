package com.ktb.howard.ktb_community_server.post.dto;

import com.ktb.howard.ktb_community_server.member.dto.MemberInfoResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@ToString
public class PostDetailDto {

    private Long postId;

    private MemberInfoResponseDto writer;

    private List<PostImageInfoDto> postImages;

    private String title;

    private String content;

    private Integer likeCount;

    private Long viewCount;

    private Long commentCount;

    private Boolean isLiked;

    private LocalDateTime createdAt;

}
