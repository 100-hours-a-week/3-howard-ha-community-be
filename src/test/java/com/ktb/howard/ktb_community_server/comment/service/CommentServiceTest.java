package com.ktb.howard.ktb_community_server.comment.service;


import com.ktb.howard.ktb_community_server.comment.domain.Comment;
import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentRequestDto;
import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentResponseDto;
import com.ktb.howard.ktb_community_server.comment.repository.CommentRepository;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class CommentServiceTest {

    @Autowired
    CommentService commentService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Member member;
    private Post post;
    private Comment parentComment;

    @BeforeEach
    void init() {
        member = Member.builder()
                .email("test@example.com")
                .password("Password12345!")
                .nickname("test.park")
                .build();
        memberRepository.save(member);
        post = Post.builder()
                .writer(member)
                .title("test title")
                .content("test content")
                .likeCount(0)
                .viewCount(0L)
                .commentCount(0L)
                .build();
        postRepository.save(post);
        parentComment = Comment.builder()
                .post(post)
                .member(member)
                .content("테스트용 댓글1")
                .build();
        commentRepository.save(parentComment);
    }

    @Test
    @DisplayName("댓글 생성 - 입력한 정보에 따라 댓글 정보를 생성한다 (대댓글X)")
    void createCommentDoNotHaveParentCommentTest() {
        // given
        CreateCommentRequestDto request = new CreateCommentRequestDto(
                post.getId(),
                member.getId(),
                null,
                "테스트용 댓글1"
        );

        // when
        CreateCommentResponseDto response = commentService.createComment(request);

        // then
        Optional<Comment> findComment = commentRepository.findById(response.commentId());
        assertThat(findComment).isPresent();
        assertThat(response)
                .extracting("commentId", "postId", "memberId", "parentCommentId", "content")
                .containsExactly(
                        findComment.get().getId(),
                        findComment.get().getPost().getId(),
                        findComment.get().getMember().getId(),
                        null,
                        findComment.get().getContent()
                );
    }

    @Test
    @DisplayName("댓글 생성 - 입력한 정보에 따라 댓글 정보를 생성한다 (대댓글O)")
    void createCommentHaveParentCommentTest() {
        // given
        CreateCommentRequestDto request = new CreateCommentRequestDto(
                post.getId(),
                member.getId(),
                parentComment.getId(),
                "테스트용 댓글2"
        );

        // when
        CreateCommentResponseDto response = commentService.createComment(request);

        // then
        Optional<Comment> findComment = commentRepository.findById(response.commentId());
        assertThat(findComment).isPresent();
        assertThat(response)
                .extracting("commentId", "postId", "memberId", "parentCommentId", "content")
                .containsExactly(
                        findComment.get().getId(),
                        findComment.get().getPost().getId(),
                        findComment.get().getMember().getId(),
                        findComment.get().getParentComment().getId(),
                        findComment.get().getContent()
                );
    }

}