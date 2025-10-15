package com.ktb.howard.ktb_community_server.post.controller;

import com.ktb.howard.ktb_community_server.auth.dto.CustomUser;
import com.ktb.howard.ktb_community_server.like_log.domain.LikeLogType;
import com.ktb.howard.ktb_community_server.post.dto.*;
import com.ktb.howard.ktb_community_server.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<CreatePostResponseDto> createPost(
            @AuthenticationPrincipal CustomUser loginMember,
            @Valid @RequestBody CreatePostRequestDto request
    ) {
        CreatePostResponseDto response = postService.createPost(
                loginMember.getId(),
                request.title(),
                request.content(),
                request.postImages()
        );
        return ResponseEntity
                .created(URI.create("/posts/" + response.postId()))
                .body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<GetPostsResponseDto>> getPosts(
            @RequestParam("cursor") Long cursor,
            @RequestParam("size") Integer size
    ) {
        List<GetPostsResponseDto> posts = postService.getPosts(cursor, size);
        return ResponseEntity.ok(posts);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailDto> getPostDetail(
            @AuthenticationPrincipal CustomUser loginMember,
            @PathVariable Long postId
    ) {
        PostDetailDto response = postService.getPostDetail(postId, loginMember.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/like/{postId}")
    public ResponseEntity<String> likePost(
            @AuthenticationPrincipal CustomUser loginMember,
            @PathVariable Long postId,
            @RequestParam("type") LikeLogType type
    ) {
        postService.likePost(postId, loginMember.getId(), type);
        return ResponseEntity.ok("");
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePostById(
            @AuthenticationPrincipal CustomUser loginMember,
            @PathVariable Long postId
    ) {
        postService.deletePostById(loginMember.getId(), postId);
        return ResponseEntity.ok("게시글이 삭제되었습니다.");
    }

}
