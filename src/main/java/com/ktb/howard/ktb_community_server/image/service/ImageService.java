package com.ktb.howard.ktb_community_server.image.service;

import com.ktb.howard.ktb_community_server.image.domain.Image;
import com.ktb.howard.ktb_community_server.image.domain.ImageStatus;
import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import com.ktb.howard.ktb_community_server.image.dto.*;
import com.ktb.howard.ktb_community_server.image.repository.ImageRepository;
import com.ktb.howard.ktb_community_server.infra.aws.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageService {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    @Value("${app.s3.bucket}")
    private String bucketName;
    @Value("${aws.region}")
    private String region;

    @Transactional
    public GetImageUploadUrlResponseDto getImageUploadUrl(GetImageUploadUrlRequestDto request) {
        List<GetImageUploadUrlResponseDto.ImageUploadResponseInfoDto> images = new ArrayList<>();
        for (GetImageUploadUrlRequestDto.ImageUploadRequestInfoDto image : request.getImages()) {
            String temporalObjectKey = generateTemporalObjectKey(request.getImageType(), image.fileName());
            String[] tokens = temporalObjectKey.split("/");
            String reservedFileName = tokens[tokens.length - 1];
            Image reserved = Image.builder()
                    .imageType(request.getImageType())
                    .bucketName(bucketName)
                    .region(region)
                    .objectKey(temporalObjectKey)
                    .fileName(reservedFileName)
                    .fileSize(image.fileSize())
                    .mimeType(image.mimeType())
                    .sequence(image.sequence())
                    .status(ImageStatus.RESERVED)
                    .build();
            imageRepository.save(reserved);
            S3Service.UploadPresignResponse uploadInfo = s3Service.getUploadUrl(temporalObjectKey, image.mimeType());
            LocalDateTime expiresAt = uploadInfo.expiresAt()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            images.add(
                    new GetImageUploadUrlResponseDto.ImageUploadResponseInfoDto(
                            uploadInfo.url(),
                            reserved.getId(),
                            expiresAt
                    )
            );
        }
        return GetImageUploadUrlResponseDto.builder()
                .images(images)
                .build();
    }

    @Transactional(readOnly = true)
    public GetImageUrlResponseDto getImageViewUrl(List<Long> request) {
        List<GetImageUrlResponseDto.ImageUrlInfoDto> images = new ArrayList<>();
        for (Long imageId : request) {
            String objectKey = imageRepository.findObjectKeyById(imageId);
            S3Service.ViewPresignResponse urlInfo = s3Service.getUrl(objectKey);
            LocalDateTime expiresAt = urlInfo.expiresAt()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            images.add(new GetImageUrlResponseDto.ImageUrlInfoDto(urlInfo.url(), expiresAt));
        }
        return GetImageUrlResponseDto.builder()
                .images(images)
                .build();
    }

    private String generateTemporalObjectKey(ImageType imageType, String originalFileName) {
        String temporalObjectKey;
        String extension = "";
        int dot = originalFileName.lastIndexOf('.');
        if (dot > -1) extension = originalFileName.substring(dot);
        if (ImageType.PROFILE.equals(imageType)) temporalObjectKey = "/tmp/profile";
        else temporalObjectKey = "/tmp/post";
        return temporalObjectKey + "/" + UUID.randomUUID() + extension;
    }

}
