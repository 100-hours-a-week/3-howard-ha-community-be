package com.ktb.howard.ktb_community_server.image.controller;

import com.ktb.howard.ktb_community_server.image.dto.CreateImageUploadUrlRequestDto;
import com.ktb.howard.ktb_community_server.image.dto.CreateImageUploadUrlResponseDto;
import com.ktb.howard.ktb_community_server.image.dto.CreateImageViewUrlRequestDto;
import com.ktb.howard.ktb_community_server.image.dto.GetImageUrlResponseDto;
import com.ktb.howard.ktb_community_server.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload-urls")
    public ResponseEntity<CreateImageUploadUrlResponseDto> createImageUploadUrl(
            @RequestBody CreateImageUploadUrlRequestDto request
    ) {
        CreateImageUploadUrlResponseDto response = imageService.createImageUploadUrl(request);
        return ResponseEntity.status(200).body(response);
    }

    @PostMapping("/view-urls")
    public ResponseEntity<GetImageUrlResponseDto> createImageViewUrl(@RequestBody CreateImageViewUrlRequestDto request) {
        GetImageUrlResponseDto response = imageService.createImageViewUrl(request);
        return ResponseEntity.status(200).body(response);
    }

}
