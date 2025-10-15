package com.ktb.howard.ktb_community_server.comment.controller;

import com.ktb.howard.ktb_community_server.auth.dto.CustomUser;
import com.ktb.howard.ktb_community_server.comment.dto.CommentResponseDto;
import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentRequestDto;
import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentResponseDto;
import com.ktb.howard.ktb_community_server.comment.dto.UpdateCommentRequestDto;
import com.ktb.howard.ktb_community_server.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
public class CommentController {

    private final CommentService commentService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comments")
    public ResponseEntity<CreateCommentResponseDto> createComment(
            @AuthenticationPrincipal CustomUser loginMember,
            @Valid @RequestBody CreateCommentRequestDto request
    ) {
        CreateCommentResponseDto response = commentService.createComment(
                request.postId(),
                loginMember.getId(),
                request.parentCommentId(),
                request.content()
        );
        return ResponseEntity
                .created(URI.create("/comments/" + response.commentId()))
                .body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{postId}/comments")
    public ResponseEntity<Page<CommentResponseDto>> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<CommentResponseDto> comments = commentService.getComments(postId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<String> updateComment(
            @AuthenticationPrincipal CustomUser loginMember,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequestDto request
    ) {
        commentService.updateComment(loginMember.getId(), commentId, request.content());
        return ResponseEntity.status(200).body("댓글이 수정되었습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @AuthenticationPrincipal CustomUser loginMember,
            @PathVariable Long commentId
    ) {
        commentService.softDeleteByCommentId(loginMember.getId(), commentId);
        return ResponseEntity.status(200).body("댓글이 삭제되었습니다.");
    }

}
