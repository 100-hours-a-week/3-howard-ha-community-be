package com.ktb.howard.ktb_community_server.image.controller;

import com.ktb.howard.ktb_community_server.image.dto.GetImageUploadUrlRequestDto;
import com.ktb.howard.ktb_community_server.image.dto.GetImageUploadUrlResponseDto;
import com.ktb.howard.ktb_community_server.image.dto.GetImageUrlResponseDto;
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

    @PostMapping("/url")
    public ResponseEntity<GetImageUploadUrlResponseDto> getImageUploadUrl(@RequestBody GetImageUploadUrlRequestDto request) {
        GetImageUploadUrlResponseDto response = imageService.getImageUploadUrl(request);
        return ResponseEntity.status(200).body(response);
    }

    @GetMapping("/url")
    public ResponseEntity<GetImageUrlResponseDto> getImageViewUrl(@RequestParam("image-id") List<Long> request) {
        GetImageUrlResponseDto response = imageService.getImageViewUrl(request);
        return ResponseEntity.status(200).body(response);
    }

}
