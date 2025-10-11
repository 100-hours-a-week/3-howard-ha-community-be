package com.ktb.howard.ktb_community_server.post.controller;

import com.ktb.howard.ktb_community_server.auth.dto.CustomUser;
import com.ktb.howard.ktb_community_server.post.dto.CreatePostRequestDto;
import com.ktb.howard.ktb_community_server.post.dto.CreatePostResponseDto;
import com.ktb.howard.ktb_community_server.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

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

}
