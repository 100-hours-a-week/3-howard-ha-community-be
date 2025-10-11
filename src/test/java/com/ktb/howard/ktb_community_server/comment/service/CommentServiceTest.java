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

import java.util.List;
import java.util.NoSuchElementException;
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

    private Member preMember;
    private Post prePost;
    private Comment preParentComment;

    @BeforeEach
    void init() {
        preMember = Member.builder()
                .email("test@example.com")
                .password("Password12345!")
                .nickname("test.park")
                .build();
        memberRepository.save(preMember);
        prePost = Post.builder()
                .writer(preMember)
                .title("test title")
                .content("test content")
                .likeCount(0)
                .viewCount(0L)
                .commentCount(0L)
                .build();
        postRepository.save(prePost);
        preParentComment = Comment.builder()
                .post(prePost)
                .member(preMember)
                .content("테스트용 댓글1")
                .build();
        commentRepository.save(preParentComment);
    }

    @Test
    @DisplayName("댓글 생성 - 입력한 정보에 따라 댓글 정보를 생성한다 (대댓글X)")
    void createCommentDoNotHaveParentCommentTest() {
        // given
        Post post = postRepository.getReferenceById(prePost.getId());
        Member member = memberRepository.getReferenceById(preMember.getId().longValue());
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
        Post post = postRepository.getReferenceById(prePost.getId());
        Member member = memberRepository.getReferenceById(preMember.getId().longValue());
        Comment parentComment = commentRepository.getReferenceById(preParentComment.getId());
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

    @Test
    @DisplayName("댓글 수정 성공 - 요청한 댓글이 존재하는 경우, 요청된 본문으로 댓글의 내용을 수정한다.")
    void updateCommentSuccessTest() {
        // given
        Post post = postRepository.getReferenceById(prePost.getId());
        Member member = memberRepository.getReferenceById(preMember.getId().longValue());
        Comment parentComment = commentRepository.getReferenceById(preParentComment.getId());
        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .parentComment(parentComment)
                .content("수정 이전의 댓글")
                .build();
        commentRepository.save(comment);

        // when
        String updatedContent = "수정된 댓글";
        commentService.updateComment(comment.getId(), updatedContent);

        // then
        Comment updatedComment = commentRepository.findById(comment.getId())
                .orElseThrow(() -> new AssertionError("댓글이 존재하지 않습니다."));
        assertThat(updatedComment.getContent()).isEqualTo(updatedContent);
    }

    @Test
    @DisplayName("댓글 수정 실패 - 요청한 댓글이 존재하지 않는 경우, 예외를 반환한다.")
    void updateCommentFailTest() {
        // given

        // when // then
        assertThatThrownBy(() -> commentService.updateComment(1L, "수정된 댓글"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage(1L + "에 대응하는 댓글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("댓글 삭제 - 댓글 삭제 시 해당 댓글만 제거하며, 해당 댓글에 포함된 대댓글은 유지한다.")
    void deleteCommentTest() {
        // given
        Post post = postRepository.getReferenceById(prePost.getId());
        Member member = memberRepository.getReferenceById(preMember.getId().longValue());
        Comment parentComment = commentRepository.getReferenceById(preParentComment.getId());
        Comment childCommentA = Comment.builder()
                .post(post)
                .member(member)
                .parentComment(parentComment)
                .content("테스트용 대댓글A")
                .build();
        Comment childCommentB = Comment.builder()
                .post(post)
                .member(member)
                .parentComment(parentComment)
                .content("테스트용 대댓글B")
                .build();
        Comment childCommentC = Comment.builder()
                .post(post)
                .member(member)
                .parentComment(parentComment)
                .content("테스트용 대댓글C")
                .build();
        commentRepository.save(childCommentA);
        commentRepository.save(childCommentB);
        commentRepository.save(childCommentC);

        // when
        commentService.softDeleteByCommentId(parentComment.getId());

        // then
        Optional<Comment> deletedComment = commentRepository.findById(parentComment.getId());
        List<Comment> childComments = commentRepository.findByParentCommentId(parentComment.getId());
        assertThat(deletedComment).isPresent().get().extracting(Comment::getDeletedAt).isNotNull();
        assertThat(childComments).hasSize(3)
                .extracting("id")
                .contains(childCommentA.getId(), childCommentB.getId(), childCommentC.getId());
    }

}