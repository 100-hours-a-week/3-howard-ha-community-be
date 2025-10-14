package com.ktb.howard.ktb_community_server.image.controller;

import com.ktb.howard.ktb_community_server.image.dto.*;
import com.ktb.howard.ktb_community_server.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload-urls")
    public ResponseEntity<List<ImageUrlResponseDto>> createImageUploadUrl(
            @RequestBody CreateImageUploadUrlRequestDto request
    ) {
        return ResponseEntity.ok(imageService.createImageUploadUrl(request));
    }

    @PostMapping("/view-urls")
    public ResponseEntity<List<ImageUrlResponseDto>> createImageViewUrl(
            @RequestBody CreateImageViewUrlRequestDto request
    ) {
        return ResponseEntity.ok(imageService.createImageViewUrl(request));
    }

}
