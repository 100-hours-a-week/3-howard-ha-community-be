package com.ktb.howard.ktb_community_server.post.service;

import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.dto.CreatePostResponseDto;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class PostServiceTest {

    @Autowired
    PostService postService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("게시글 생성 - 게시글 이미지를 첨부하지 않은 경우")
    void createPostWithNoPostImageTest() {
        // given
        Member writer = Member.builder()
                .email("test@example.com")
                .password("Password12345!")
                .nickname("test.park")
                .build();
        memberRepository.save(writer);

        // when
        CreatePostResponseDto response = postService.createPost(
                writer.getId(),
                "테스트 제목1",
                "테스트용 게시글 본문1",
                null
        );

        // then
        Post createdPost = postRepository.findById(response.postId())
                        .orElseThrow(() -> new AssertionError("게시글이 생성되지 않았습니다."));
        assertThat(createdPost)
                .extracting("id", "writer.id", "title", "content", "likeCount", "viewCount", "commentCount")
                .containsExactly(
                        response.postId(),
                        response.writerId(),
                        response.title(),
                        response.content(),
                        0,
                        0L,
                        0L
                );
    }

}