package com.ktb.howard.ktb_community_server.infra.aws.s3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class S3Service {

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

    public record UploadPresignResponse(String url, java.time.Instant expiresAt) {}
    public record ViewPresignResponse(String url, java.time.Instant expiresAt) {}

}
