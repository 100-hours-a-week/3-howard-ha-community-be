package com.ktb.howard.ktb_community_server.comment.service;

import com.ktb.howard.ktb_community_server.comment.domain.Comment;
import com.ktb.howard.ktb_community_server.comment.dto.CommentResponseDto;
import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentResponseDto;
import com.ktb.howard.ktb_community_server.comment.repository.CommentRepository;
import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import com.ktb.howard.ktb_community_server.image.dto.CreateImageViewUrlRequestDto;
import com.ktb.howard.ktb_community_server.image.service.ImageService;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.dto.MemberInfoResponseDto;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.member.service.MemberService;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final MemberService memberService;
    private final CommentRepository commentRepository;
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
        Comment savedComment = commentRepository.save(comment);
        post.increaseCommentCount(); // 댓글 수 1증가
        return new CreateCommentResponseDto(
                savedComment.getId(),
                savedComment.getPost().getId(),
                savedComment.getMember().getId(),
                parentComment != null ? savedComment.getParentComment().getId() : null,
                savedComment.getContent()
        );
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long postId, Long cursor, Integer size) {
        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<Comment> comments;
        if (cursor == 0) {
            comments = commentRepository.findComments(postId, pageRequest);
        } else {
            comments = commentRepository.findCommentsNextPage(postId, cursor, pageRequest);
        }
        return comments.stream()
                .map(c -> {
                    MemberInfoResponseDto profile = memberService
                            .getProfile(c.getMember().getId(), c.getMember().getEmail(), c.getMember().getNickname());
                    return new CommentResponseDto(
                            c,
                            profile.profileImageUrl()
                    );
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getChildComments(Long parentCommentId) {
        return commentRepository.findByParentCommentId(parentCommentId).stream()
                .map(c -> {
                    String profileImageUrl = imageService.createImageViewUrl(
                            new CreateImageViewUrlRequestDto(ImageType.PROFILE, c.getMember().getId().longValue())
                    ).getFirst().url();
                    return new CommentResponseDto(c, profileImageUrl);
                })
                .toList();
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
        findComment.getPost().decreaseCommentCount(); // 댓글 갯수 1감소
        findComment.updateDeletedAt(LocalDateTime.now());
    }

}
