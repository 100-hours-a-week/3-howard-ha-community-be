package com.ktb.howard.ktb_community_server.image.service;

import com.google.common.base.Strings;
import com.ktb.howard.ktb_community_server.image.domain.Image;
import com.ktb.howard.ktb_community_server.image.domain.ImageStatus;
import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import com.ktb.howard.ktb_community_server.image.dto.*;
import com.ktb.howard.ktb_community_server.image.exception.*;
import com.ktb.howard.ktb_community_server.image.repository.ImageRepository;
import com.ktb.howard.ktb_community_server.infra.aws.s3.dto.PresignedUrl;
import com.ktb.howard.ktb_community_server.infra.aws.s3.service.S3Service;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.io.Files;

import java.util.*;

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
    public Image createImage(ImageType type, ImageMetadata metadata, ImageStatus status) {
        GenerateObjectKeyResponse objectKey = generateObjectKey(type, metadata.fileName(), status);
        Image image = Image.builder()
                .imageType(type)
                .bucketName(bucketName)
                .region(region)
                .objectKey(objectKey.objectKey())
                .fileName(objectKey.fileName())
                .fileSize(metadata.fileSize())
                .mimeType(metadata.mimeType())
                .sequence(metadata.sequence())
                .status(status)
                .build();
        imageRepository.save(image);
        return image;
    }

    @Transactional
    public List<ImageUrlResponseDto> createImageUploadUrl(CreateImageUploadUrlRequestDto request) {
        List<ImageUrlResponseDto> response = new ArrayList<>();
        // 업로드를 시도하는 최대 이미지 갯수에 대한 체크를 선행 - PROFILE: 1장, POST: 5장으로 제한
        int imageCount = request.getImageMetadataList().size();
        if (ImageType.PROFILE.equals(request.getImageType()) && imageCount > 1
                || ImageType.POST.equals(request.getImageType()) && imageCount > 5) {
            log.error("이미지 수량한도 초과! : 유형={}, 수량={}", request.getImageType(), imageCount);
            throw new InvalidImageCountException("프로필 이미지는 최대 1장, 게시글 이미지는 최대 5장까지 가능합니다.");
        }
        for (ImageMetadata image : request.getImageMetadataList()) {
            // MIME Type에 대한 체크를 선행
            String mimeType = image.mimeType().toLowerCase();
            if (!mimeType.startsWith("image/")) {
                log.error("지원하지 않는 파일 형식 : mimeType={}", mimeType);
                throw new InvalidMimeTypeException("지원하지 않는 파일 형식입니다.", image.mimeType());
            }
            // 이미지 파일 용량에 대한 체크를 선행 - 업로드 가능한 최대 이미지 용량을 1MB로 제한
            if (image.fileSize() > 1024 * 1024) {
                log.error("이미지 용량 한도 초과! : 현재 용량={}byte", image.fileSize());
                throw new ImageSizeExceededException(
                        "이미지 파일의 용량은 1MB를 초과할 수 없습니다.",
                        image.fileSize(),
                        image.mimeType()
                );
            }
            Image createdImage = createImage(request.getImageType(), image, ImageStatus.RESERVED);
            PresignedUrl presignedUrl = s3Service.createPutObjectPresignedUrl(
                    createdImage.getObjectKey(),
                    createdImage.getMimeType(),
                    createdImage.getFileSize()
            );
            response.add(new ImageUrlResponseDto(
                    presignedUrl.presignedUrl(),
                    createdImage.getId(),
                    createdImage.getSequence(),
                    presignedUrl.httpMethod(),
                    presignedUrl.expiresAt()
            ));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<ImageUrlResponseDto> createImageViewUrl(CreateImageViewUrlRequestDto request) {
        return imageRepository.findImageByImageTypeAndReferenceId(request.getImageType(), request.getReferenceId())
                .stream()
                .map(image -> {
                    PresignedUrl presignedUrl = s3Service.createGetObjectPresignedUrl(image.getObjectKey());
                    return new ImageUrlResponseDto(
                            presignedUrl.presignedUrl(),
                            image.getId(),
                            image.getSequence(),
                            presignedUrl.httpMethod(),
                            presignedUrl.expiresAt()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Boolean isExist(Long imageId) {
        String objectKey = imageRepository.findObjectKeyById(imageId);
        if (objectKey == null) {
            log.error("imageId {}가 존재하지 않는 값으로, objectKey 생성 실패...", imageId);
            throw new IllegalArgumentException("imageId가 존재하지 않는 값으로, objectKey 생성 실패...");
        }
        log.info("objectKey : {}", objectKey);
        return s3Service.getObjectMetaData(objectKey).isPresent();
    }

    @Transactional
    public void persistImage(Long imageId, Member owner, Long referenceId) {
        Optional<Image> imageOpt = imageRepository.findById(imageId);
        if (imageOpt.isEmpty()) {
            log.error("존재하지 않은 이미지: imageId={}, ownerId={}, referenceId={}", imageId, owner.getId(), referenceId);
            throw new ImageNotFoundException("존재하지 않는 이미지입니다.", imageId, referenceId);
        }
        Image image = imageOpt.get();
        GenerateObjectKeyResponse persistObjectKey = generateObjectKey(
                image.getImageType(),
                image.getFileName(),
                ImageStatus.PERSIST
        );
        s3Service.moveObject(image.getObjectKey(), persistObjectKey.objectKey());
        image.updateOwner(owner);
        image.updateReference(referenceId);
        image.updateObjectKey(persistObjectKey.objectKey());
        image.updateStatus(ImageStatus.PERSIST);
    }

    public GenerateObjectKeyResponse generateObjectKey(ImageType imageType, String originalFileName, ImageStatus status) {
        String objectKey;
        String fileName;
        String extension = Files.getFileExtension(originalFileName);
        if (Strings.isNullOrEmpty(extension)) {
            log.error("파일 확장자 추출 실패 : {}", originalFileName);
            throw new FileExtensionExtractionFailedException("확장자 추출 실패 -> " + originalFileName, originalFileName);
        }
        if (ImageStatus.RESERVED.equals(status)) {
            fileName = UUID.randomUUID() + "." + extension;
            objectKey = (ImageType.PROFILE.equals(imageType) ? "tmp/profiles/" : "tmp/posts/") + fileName;
        } else if (ImageStatus.PERSIST.equals(status)) {
            fileName = originalFileName;
            objectKey = (ImageType.PROFILE.equals(imageType) ? "profiles/" : "posts/") + fileName;
        } else {
            log.error("Object Key 생성 불가 상태값 = {}", status.toString());
            throw new InvalidImageStatusException("Object Key 생성을 할 수 있는 상태값이 아닙니다.", status);
        }
        log.info("ObjectKey 생성 : fileName={}, objectKey={}", fileName, objectKey);
        return new GenerateObjectKeyResponse(objectKey, fileName);
    }

}
