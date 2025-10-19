package com.ktb.howard.ktb_community_server.comment.service;

import com.ktb.howard.ktb_community_server.comment.domain.Comment;
import com.ktb.howard.ktb_community_server.comment.dto.CommentResponseDto;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

        // when
        CreateCommentResponseDto response = commentService.createComment(
                post.getId(),
                member.getId(),
                null,
                "테스트용 댓글1"
        );

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

        // when
        CreateCommentResponseDto response = commentService.createComment(
                post.getId(),
                member.getId(),
                parentComment.getId(),
                "테스트용 댓글2"
        );

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
    @DisplayName("댓글 목록 조회 - 커서의 값이 0인 경우, 가장 최근 댓글을 조회하여 반환한다.")
    void getCommentsNoCursorTest() throws InterruptedException {
        // given
        Member writer = Member.builder()
                .email("test1@example.com")
                .password("Password12345!")
                .nickname("test1.park")
                .build();
        memberRepository.save(writer);
        Post post = Post.builder()
                .writer(writer)
                .title("테스트 제목1")
                .content("테스트용 게시글 본문1")
                .build();
        postRepository.save(post);
        List<Comment> commentList = new ArrayList<>();
        for (int i=1 ; i<=20 ; i++) {
            Comment comment = Comment.builder()
                    .post(post)
                    .member(writer)
                    .content("테스트용 댓글 " + i)
                    .build();
            commentList.add(comment);
            commentRepository.save(comment);
            Thread.sleep(500);
        }

        // when
        List<CommentResponseDto> comments = commentService.getComments(post.getId(), 0L, 3);

        // then
        assertThat(comments).hasSize(3)
                .extracting(
                        "commentId",
                        "content",
                        "writerInfo.email",
                        "writerInfo.nickname",
                        "createdAt"
                )
                .containsExactly(
                        tuple(
                                commentList.get(19).getId(),
                                commentList.get(19).getContent(),
                                commentList.get(19).getMember().getEmail(),
                                commentList.get(19).getMember().getNickname(),
                                commentList.get(19).getCreatedAt()
                        ),
                        tuple(
                                commentList.get(18).getId(),
                                commentList.get(18).getContent(),
                                commentList.get(18).getMember().getEmail(),
                                commentList.get(18).getMember().getNickname(),
                                commentList.get(18).getCreatedAt()
                        ),
                        tuple(
                                commentList.get(17).getId(),
                                commentList.get(17).getContent(),
                                commentList.get(17).getMember().getEmail(),
                                commentList.get(17).getMember().getNickname(),
                                commentList.get(17).getCreatedAt()
                        )
                );
    }

    @Test
    @DisplayName("댓글 목록 조회 - 커서의 값이 0이 아닌 경우, 해당 커서 이후의 값을 조회하여 반환한다.")
    void getCommentsWithCursorTest() throws InterruptedException {
        // given
        Member writer = Member.builder()
                .email("test1@example.com")
                .password("Password12345!")
                .nickname("test1.park")
                .build();
        memberRepository.save(writer);
        Post post = Post.builder()
                .writer(writer)
                .title("테스트 제목1")
                .content("테스트용 게시글 본문1")
                .build();
        postRepository.save(post);
        List<Comment> commentList = new ArrayList<>();
        for (int i=1 ; i<=20 ; i++) {
            Comment comment = Comment.builder()
                    .post(post)
                    .member(writer)
                    .content("테스트용 댓글 " + i)
                    .build();
            commentList.add(comment);
            commentRepository.save(comment);
            Thread.sleep(500);
        }

        // when
        List<CommentResponseDto> comments = commentService.getComments(post.getId(), commentList.get(15).getId(), 3);

        // then
        assertThat(comments).hasSize(3)
                .extracting(
                        "commentId",
                        "content",
                        "writerInfo.email",
                        "writerInfo.nickname",
                        "createdAt"
                )
                .containsExactly(
                        tuple(
                                commentList.get(14).getId(),
                                commentList.get(14).getContent(),
                                commentList.get(14).getMember().getEmail(),
                                commentList.get(14).getMember().getNickname(),
                                commentList.get(14).getCreatedAt()
                        ),
                        tuple(
                                commentList.get(13).getId(),
                                commentList.get(13).getContent(),
                                commentList.get(13).getMember().getEmail(),
                                commentList.get(13).getMember().getNickname(),
                                commentList.get(13).getCreatedAt()
                        ),
                        tuple(
                                commentList.get(12).getId(),
                                commentList.get(12).getContent(),
                                commentList.get(12).getMember().getEmail(),
                                commentList.get(12).getMember().getNickname(),
                                commentList.get(12).getCreatedAt()
                        )
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
        commentService.updateComment(member.getId(), comment.getId(), updatedContent);

        // then
        Comment updatedComment = commentRepository.findById(comment.getId())
                .orElseThrow(() -> new AssertionError("댓글이 존재하지 않습니다."));
        assertThat(updatedComment.getContent()).isEqualTo(updatedContent);
    }

    @Test
    @DisplayName("댓글 수정 실패 - 요청한 댓글에 대한 권한이 없는 사용자가 수정을 요청한 경우, 예외를 반환한다.")
    void updateCommentFailWhenNotAuthorizedTest() {
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

        // when // then
        String updatedContent = "수정된 댓글";
        assertThatThrownBy(() -> commentService.updateComment(member.getId() + 1, comment.getId(), updatedContent))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("올바르지 않은 요청입니다.");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 요청한 댓글이 존재하지 않는 경우, 예외를 반환한다.")
    void updateCommentFailWhenCommentNotExistTest() {
        // given
        Member member = memberRepository.getReferenceById(preMember.getId().longValue());

        // when // then
        assertThatThrownBy(() -> commentService.updateComment(member.getId(), 1L, "수정된 댓글"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage(1L + "에 대응하는 댓글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("댓글 삭제 성공 - 댓글 삭제 시 해당 댓글만 제거하며, 해당 댓글에 포함된 대댓글은 유지한다.")
    void deleteCommentSuccessTest() {
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
        commentService.softDeleteByCommentId(member.getId(), parentComment.getId());

        // then
        Optional<Comment> deletedComment = commentRepository.findById(parentComment.getId());
        List<Comment> childComments = commentRepository.findByParentCommentId(parentComment.getId());
        assertThat(deletedComment).isPresent().get().extracting(Comment::getDeletedAt).isNotNull();
        assertThat(childComments).hasSize(3)
                .extracting("id")
                .contains(childCommentA.getId(), childCommentB.getId(), childCommentC.getId());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 요청한 댓글에 대한 권한이 없는 사용자가 삭제를 요청한 경우, 예외를 반환한다.")
    void deleteCommentFailWhenNotAuthorizedTest() {
        Member member = memberRepository.getReferenceById(preMember.getId().longValue());
        Comment parentComment = commentRepository.getReferenceById(preParentComment.getId());

        // when // then
        assertThatThrownBy(() -> commentService.softDeleteByCommentId(member.getId() + 1, parentComment.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("올바르지 않은 요청입니다.");
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 요청한 댓글이 존재하지 않는 경우, 예외를 반환한다.")
    void deleteCommentFailWhenCommentNotExistTest() {
        Member member = memberRepository.getReferenceById(preMember.getId().longValue());
        Comment parentComment = commentRepository.getReferenceById(preParentComment.getId());

        // when // then
        assertThatThrownBy(() -> commentService.softDeleteByCommentId(member.getId(), parentComment.getId() + 1))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage((parentComment.getId() + 1) + "에 대응하는 댓글을 찾을 수 없습니다.");
    }

}