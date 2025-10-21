package com.ktb.howard.ktb_community_server.comment.service.unit;

import com.ktb.howard.ktb_community_server.comment.domain.Comment;
import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentResponseDto;
import com.ktb.howard.ktb_community_server.comment.repository.CommentRepository;
import com.ktb.howard.ktb_community_server.comment.service.CommentService;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    CommentRepository commentRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    CommentService commentService;

    Post mockPost;
    Member mockMember;
    Comment mockParentComment;

    @BeforeEach
    void init() {
        mockPost = mock(Post.class);
        when(mockPost.getId()).thenReturn(1L);
        mockMember = mock(Member.class);
        when(mockMember.getId()).thenReturn(1);
    }

    @Test
    @DisplayName("댓글 생성 - 대댓글이 아닌 경우, 입력된 정보를 바탕으로 댓글을 생성한다.")
    void createCommentSuccessTest() {
        // given
        Long postId = 1L;
        Integer memberId = 1;
        Long parentCommentId = null;
        String content = "테스트용 댓글";

        when(postRepository.getReferenceById(postId)).thenReturn(mockPost);
        when(memberRepository.getReferenceById(memberId.longValue())).thenReturn(mockMember);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment commentToSave = invocation.getArgument(0);
            Comment comment = mock(Comment.class);
            when(comment.getId()).thenReturn(100L);
            when(comment.getContent()).thenReturn(commentToSave.getContent());
            when(comment.getPost()).thenReturn(commentToSave.getPost());
            when(comment.getMember()).thenReturn(commentToSave.getMember());
            return comment;
        });

        // when
        CreateCommentResponseDto response = commentService.createComment(postId, memberId, parentCommentId, content);

        // then
        assertThat(response).isNotNull()
                .extracting("commentId", "postId", "memberId", "parentCommentId", "content")
                .containsExactly(100L, postId, memberId, null, content);
        verify(postRepository).getReferenceById(postId);
        verify(memberRepository).getReferenceById(memberId.longValue());
        verify(commentRepository, never()).getReferenceById(anyLong());
        verify(commentRepository).save(any(Comment.class));
        verify(mockPost).increaseCommentCount();
    }

}
