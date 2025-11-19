package com.ktb.howard.ktb_community_server.image.controller;

import com.ktb.howard.ktb_community_server.api.ApiResponse;
import com.ktb.howard.ktb_community_server.image.dto.*;
import com.ktb.howard.ktb_community_server.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload-urls")
    public ResponseEntity<ApiResponse<List<ImageUrlResponseDto>>> createImageUploadUrl(
            @RequestBody CreateImageUploadUrlRequestDto request
    ) {
        List<ImageUrlResponseDto> imageUploadUrls = imageService.createImageUploadUrl(request);
        ApiResponse<List<ImageUrlResponseDto>> response = ApiResponse.onSuccess(imageUploadUrls);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/view-urls")
    public ResponseEntity<ApiResponse<List<ImageUrlResponseDto>>> createImageViewUrl(
            @RequestBody CreateImageViewUrlRequestDto request
    ) {
        List<ImageUrlResponseDto> imageViewUrls = imageService.createImageViewUrl(request);
        ApiResponse<List<ImageUrlResponseDto>> response = ApiResponse.onSuccess(imageViewUrls);
        return ResponseEntity.ok(response);
    }

}
