package com.ktb.howard.ktb_community_server.image.service;

import com.ktb.howard.ktb_community_server.image.domain.Image;
import com.ktb.howard.ktb_community_server.image.domain.ImageStatus;
import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import com.ktb.howard.ktb_community_server.image.dto.*;
import com.ktb.howard.ktb_community_server.image.repository.ImageRepository;
import com.ktb.howard.ktb_community_server.infra.aws.s3.service.S3Service;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
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

    @Transactional(readOnly = true)
    public Boolean isExist(Long imageId) {
        String objectKey = imageRepository.findObjectKeyById(imageId);
        if (objectKey == null) {
            log.error("imageId {}가 존재하지 않는 값으로, objectKey 생성 실패...", imageId);
            throw new IllegalArgumentException("imageId가 존재하지 않는 값으로, objectKey 생성 실패...");
        }
        log.info("objectKey : {}", objectKey);
        return s3Service.doesObjectExist(objectKey);
    }

    @Transactional
    public void persistImage(Long imageId, Member owner, Long referenceId) {
        Optional<Image> imageOpt = imageRepository.findById(imageId);
        if (imageOpt.isEmpty()) {
            throw new IllegalStateException("존재하지 않는 이미지입니다.");
        }
        Image image = imageOpt.get();
        String persistObjectKey;
        if (ImageType.PROFILE.equals(image.getImageType())) {
            persistObjectKey = "profiles/" + image.getId() + "/" + image.getFileName();
        } else if (ImageType.POST.equals(image.getImageType())) {
            persistObjectKey = "posts/" + image.getId() + "/" + image.getFileName();
        } else {
            log.error("유효하지 않은 이미지 타입 : {}", image.getImageType());
            throw new IllegalStateException("이미지 타입이 유효하지 않습니다.");
        }
        s3Service.moveObject(image.getObjectKey(), persistObjectKey);
        image.updateOwner(owner);
        image.updateReference(referenceId);
        image.updateObjectKey(persistObjectKey);
        image.updateStatus(ImageStatus.PERSIST);
        imageRepository.save(image);
    }

    @Transactional(readOnly = true)
    public Long getMemberProfileImageId(Integer memberId) {
        return imageRepository.findImageIdByImageTypeAndOwner(ImageType.PROFILE, memberId);
    }

    private String generateTemporalObjectKey(ImageType imageType, String originalFileName) {
        String temporalObjectKey;
        String extension = "";
        int dot = originalFileName.lastIndexOf('.');
        if (dot > -1) extension = originalFileName.substring(dot);
        if (ImageType.PROFILE.equals(imageType)) {
            temporalObjectKey = "tmp/profiles";
        } else if (ImageType.POST.equals(imageType)) {
            temporalObjectKey = "tmp/posts";
        } else {
            log.error("유효하지 않은 이미지 타입 : {}", imageType);
            throw new IllegalStateException("이미지 타입이 유효하지 않습니다.");
        }
        return temporalObjectKey + "/" + UUID.randomUUID() + extension;
    }

}
