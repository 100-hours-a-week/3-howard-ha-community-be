package com.ktb.howard.ktb_community_server.comment.service;

import com.ktb.howard.ktb_community_server.comment.domain.Comment;
import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentRequestDto;
import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentResponseDto;
import com.ktb.howard.ktb_community_server.comment.repository.CommentRepository;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CreateCommentResponseDto createComment(CreateCommentRequestDto request) {
        Post post = postRepository.getReferenceById(request.postId());
        Member member = memberRepository.getReferenceById(request.memberId().longValue());
        Comment parentComment = null;
        if (request.parentCommentId() != null) {
            parentComment = commentRepository.getReferenceById(request.parentCommentId());
        }
        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .parentComment(parentComment)
                .content(request.content())
                .build();
        commentRepository.save(comment);
        return new CreateCommentResponseDto(
                comment.getId(),
                comment.getPost().getId(),
                comment.getMember().getId(),
                parentComment != null ? comment.getParentComment().getId() : null,
                comment.getContent()
        );
    }

    @Transactional
    public void updateComment(Long commentId, String content) {
        Comment findComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException(commentId + "에 대응하는 댓글을 찾을 수 없습니다."));
        findComment.updateContent(content);
    }

    @Transactional
    public void softDeleteByCommentId(Long commentId) {
        commentRepository.softDeleteByCommentId(commentId);
    }

}
