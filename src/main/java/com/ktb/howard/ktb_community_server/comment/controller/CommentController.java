package com.ktb.howard.ktb_community_server.comment.controller;

import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentRequestDto;
import com.ktb.howard.ktb_community_server.comment.dto.CreateCommentResponseDto;
import com.ktb.howard.ktb_community_server.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CreateCommentResponseDto> createComment(@Valid @RequestBody CreateCommentRequestDto request) {
        CreateCommentResponseDto response = commentService.createComment(request);
        return ResponseEntity.status(200).body(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {
        commentService.softDeleteByCommentId(commentId);
        return ResponseEntity.status(200).body("댓글이 삭제되었습니다.");
    }

}
