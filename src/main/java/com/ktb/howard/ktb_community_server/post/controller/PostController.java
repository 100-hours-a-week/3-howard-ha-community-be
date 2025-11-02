package com.ktb.howard.ktb_community_server.post.controller;

import com.ktb.howard.ktb_community_server.api.ApiResponse;
import com.ktb.howard.ktb_community_server.auth.annotation.AuthMember;
import com.ktb.howard.ktb_community_server.auth.dto.AuthResponseDto;
import com.ktb.howard.ktb_community_server.like_log.domain.LikeLogType;
import com.ktb.howard.ktb_community_server.post.dto.*;
import com.ktb.howard.ktb_community_server.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreatePostResponseDto>> createPost(
            @AuthMember AuthResponseDto responseDto,
            @Valid @RequestBody CreatePostRequestDto request
    ) {
        CreatePostResponseDto response = postService.createPost(
                responseDto.getMemberId(),
                request.title(),
                request.content(),
                request.postImages()
        );
        ApiResponse<CreatePostResponseDto> responseBody = ApiResponse.onSuccess(response);
        return ResponseEntity
                .created(URI.create("/posts/" + response.postId()))
                .body(responseBody);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GetPostsResponseDto>>> getPosts(
            @RequestParam("cursor") Long cursor,
            @RequestParam("size") Integer size
    ) {
        List<GetPostsResponseDto> posts = postService.getPosts(cursor, size);
        ApiResponse<List<GetPostsResponseDto>> response = ApiResponse.onSuccess(posts);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailDto>> getPostDetail(
            @AuthMember AuthResponseDto responseDto,
            @PathVariable Long postId
    ) {
        PostDetailDto response = postService.getPostDetail(postId, responseDto.getMemberId());
        ApiResponse<PostDetailDto> responseBody = ApiResponse.onSuccess(response);
        return ResponseEntity.ok(responseBody);
    }

    @PatchMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<String>> likePost(
            @AuthMember AuthResponseDto responseDto,
            @PathVariable Long postId,
            @RequestParam("type") LikeLogType type
    ) {
        postService.likePost(postId, responseDto.getMemberId(), type);
        ApiResponse<String> response = ApiResponse.onSuccess("게시글 좋아요 정보를 반영했습니다.");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<String>> updatePost(
            @AuthMember AuthResponseDto responseDto,
            @PathVariable Long postId,
            @RequestBody PostUpdateRequestDto request
    ) {
        postService.updatePost(
                responseDto.getMemberId(),
                postId,
                request.getTitle(),
                request.getContent(),
                request.getImages()
        );
        ApiResponse<String> response = ApiResponse.onSuccess("게시글을 수정했습니다.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<String>> deletePostById(
            @AuthMember AuthResponseDto responseDto,
            @PathVariable Long postId
    ) {
        postService.deletePostById(responseDto.getMemberId(), postId);
        ApiResponse<String> response = ApiResponse.onSuccess("게시글이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

}
