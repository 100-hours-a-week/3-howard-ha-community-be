package com.ktb.howard.ktb_community_server.comment.controller;

import com.ktb.howard.ktb_community_server.auth.annotation.AuthMember;
import com.ktb.howard.ktb_community_server.auth.domain.Session;
import com.ktb.howard.ktb_community_server.auth.dto.AuthResponseDto;
import com.ktb.howard.ktb_community_server.comment.dto.CommentResponseDto;
import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentRequestDto;
import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentResponseDto;
import com.ktb.howard.ktb_community_server.comment.dto.UpdateCommentRequestDto;
import com.ktb.howard.ktb_community_server.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CreateCommentResponseDto> createComment(
            @AuthMember AuthResponseDto responseDto,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequestDto request
    ) {
        CreateCommentResponseDto response = commentService.createComment(
                postId,
                responseDto.getMemberId(),
                request.parentCommentId(),
                request.content()
        );
        return ResponseEntity
                .created(URI.create("/comments/" + response.commentId()))
                .body(response);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @PathVariable Long postId,
            @RequestParam("cursor") Long cursor,
            @RequestParam("size") Integer size
    ) {
        List<CommentResponseDto> comments = commentService.getComments(postId, cursor, size);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<List<CommentResponseDto>> getChildComments(@PathVariable Long commentId) {
        List<CommentResponseDto> response = commentService.getChildComments(commentId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<String> updateComment(
            @AuthMember AuthResponseDto responseDto,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequestDto request
    ) {
        commentService.updateComment(responseDto.getMemberId(), commentId, request.content());
        return ResponseEntity.status(200).body("댓글이 수정되었습니다.");
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @AuthMember AuthResponseDto responseDto,
            @PathVariable Long commentId
    ) {
        commentService.softDeleteByCommentId(responseDto.getMemberId(), commentId);
        return ResponseEntity.status(200).body("댓글이 삭제되었습니다.");
    }

}
