package com.ktb.howard.ktb_community_server.comment.service;

import com.ktb.howard.ktb_community_server.comment.domain.Comment;
import com.ktb.howard.ktb_community_server.comment.dto.CommentResponseDto;
import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentResponseDto;
import com.ktb.howard.ktb_community_server.comment.repository.CommentQueryRepository;
import com.ktb.howard.ktb_community_server.comment.repository.CommentRepository;
import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import com.ktb.howard.ktb_community_server.image.dto.CreateImageViewUrlRequestDto;
import com.ktb.howard.ktb_community_server.image.service.ImageService;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentQueryRepository commentQueryRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;

    @Transactional
    public CreateCommentResponseDto createComment(
            Long postId,
            Integer memberId,
            Long parentCommentId,
            String content
    ) {
        Post post = postRepository.getReferenceById(postId);
        Member member = memberRepository.getReferenceById(memberId.longValue());
        Comment parentComment = null;
        if (parentCommentId != null) {
            parentComment = commentRepository.getReferenceById(parentCommentId);
        }
        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .parentComment(parentComment)
                .content(content)
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

    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getComments(Long postId, Pageable pageable) {
        return commentQueryRepository.findCommentsByPostId(postId, pageable)
                .map(c -> {
                    String profileImageUrl = imageService.createImageViewUrl(
                            new CreateImageViewUrlRequestDto(ImageType.PROFILE, c.getMember().getId().longValue())
                    ).getFirst().url();
                    return new CommentResponseDto(c, profileImageUrl);
                });
    }

    @Transactional
    public void updateComment(Integer loginMemberId, Long commentId, String content) {
        Comment findComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException(commentId + "에 대응하는 댓글을 찾을 수 없습니다."));
        if (!loginMemberId.equals(findComment.getMember().getId())) {
            throw new AccessDeniedException("올바르지 않은 요청입니다.");
        }
        findComment.updateContent(content);
    }

    @Transactional
    public void softDeleteByCommentId(Integer loginMemberId, Long commentId) {
        Comment findComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException(commentId + "에 대응하는 댓글을 찾을 수 없습니다."));
        if (!loginMemberId.equals(findComment.getMember().getId())) {
            throw new AccessDeniedException("올바르지 않은 요청입니다.");
        }
        findComment.updateDeletedAt(LocalDateTime.now());
    }

}
