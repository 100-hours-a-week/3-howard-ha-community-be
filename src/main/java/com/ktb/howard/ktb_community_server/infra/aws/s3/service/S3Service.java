package com.ktb.howard.ktb_community_server.infra.aws.s3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.s3.bucket}")
    private String bucket;
    @Value("${app.s3.read-ttl}")
    private Integer readTtl;
    @Value("${app.s3.put-ttl}")
    private Integer putTtl;

    public UploadPresignResponse getUploadUrl(String key, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(readTtl))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
        return new UploadPresignResponse(presigned.url().toString(), presigned.expiration());
    }

    public ViewPresignResponse getUrl(String key) {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presign = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(putTtl))
                .getObjectRequest(get)
                .build();

        PresignedGetObjectRequest req = s3Presigner.presignGetObject(presign);
        return new ViewPresignResponse(req.url().toString(), req.expiration());
    }

    public boolean doesObjectExist(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public void moveObject(String sourceObjectKey, String destinationObjectKey) {
        log.info("이미지 이동 시작 : {} -> {}", sourceObjectKey, destinationObjectKey);
        try {
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(sourceObjectKey)
                    .destinationBucket(bucket)
                    .destinationKey(destinationObjectKey)
                    .build();
            s3Client.copyObject(copyRequest);
            log.info("이미지 복사 완료 : {}", destinationObjectKey);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(sourceObjectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("이미지 삭제 완료 : {}", sourceObjectKey);
        } catch (S3Exception e) {
            log.error("이미지 이동 중 오류 발생: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("이미지 이동 실패", e);
        }
    }

    public record UploadPresignResponse(String url, java.time.Instant expiresAt) {}
    public record ViewPresignResponse(String url, java.time.Instant expiresAt) {}

}
