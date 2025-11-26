package com.ktb.howard.ktb_community_server.image.controller;

import com.ktb.howard.ktb_community_server.api.ApiResponse;
import com.ktb.howard.ktb_community_server.image.domain.Image;
import com.ktb.howard.ktb_community_server.image.dto.*;
import com.ktb.howard.ktb_community_server.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.ktb.howard.ktb_community_server.api.AuthErrorCode.AUTH_ARGUMENT_NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;
    @Value("${aws.lambda.secret-key}")
    private String lambdaSecretKey;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createImage(
            @RequestHeader(value = "x-lambda-secret-key", required = false) String lambdaSecretKey,
            @RequestBody CreateImageRequestDto request
    ) {
        // lambda secret key 검증
        if (!this.lambdaSecretKey.equals(lambdaSecretKey)) {
            ApiResponse<String> response = ApiResponse.onFailure(AUTH_ARGUMENT_NOT_FOUND);
            return ResponseEntity.status(401).body(response);
        }
        Image createdImage = imageService.createImage(
                request.type(),
                new ImageMetadata(request.fileName(), request.fileSize(), request.mimeType(), request.sequence()),
                request.imageStatus()
        );
        ApiResponse<String> response = ApiResponse.onSuccess(String.format("이미지 %d 정보 추가완료", createdImage.getId()));
        return ResponseEntity.ok(response);
    }

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
