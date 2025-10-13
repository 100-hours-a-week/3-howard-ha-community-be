package com.ktb.howard.ktb_community_server.infra.aws.s3.service;

import com.ktb.howard.ktb_community_server.infra.aws.s3.dto.ObjectMetadata;
import com.ktb.howard.ktb_community_server.infra.aws.s3.dto.PresignedUrl;
import com.ktb.howard.ktb_community_server.infra.aws.s3.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(s3Service, "bucket", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "getTtlMin", 10);
        ReflectionTestUtils.setField(s3Service, "putTtlMin", 10);
    }

    @Test
    @DisplayName("Put Presigned URL 발급 성공 - 파일을 업로드할 수 있는 Presigned Url을 발급한다.")
    void createPutObjectPresignedUrlSuccessTest() throws Exception {
        // given
        String objectKey = "test-object-key.jpeg";
        String contentType = "image/jpeg";
        Long contentLength = 1024L;
        // Mock 객체 응답값 설정
        URL testUrl = new URI("https://test-bucket.s3.amazonaws.com/test-upload.jpg?presigned").toURL();
        Instant expiration = Instant.now().plus(Duration.ofMinutes(10));
        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(testUrl);
        when(mockPresignedRequest.expiration()).thenReturn(expiration);
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // when
        PresignedUrl response = s3Service.createPutObjectPresignedUrl(objectKey, contentType, contentLength);

        // then
        assertThat(response).isNotNull();
        assertThat(response.presignedUrl()).isEqualTo(testUrl.toString());
        assertThat(response.httpMethod()).isEqualTo(HttpMethod.PUT);
        assertThat(response.expiresAt()).isEqualTo(expiration);
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        PutObjectPresignRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.signatureDuration()).isEqualTo(Duration.ofMinutes(10));
        assertThat(capturedRequest.putObjectRequest().bucket()).isEqualTo("test-bucket");
        assertThat(capturedRequest.putObjectRequest().key()).isEqualTo(objectKey);
    }

    @Test
    @DisplayName("Put Presigned URL 발급 실패 - S3 통신 중 오류가 발생하면 FileStorageException을 반환한다.")
    void createPutObjectPresignedUrlSuccessFailTest() {
        // given
        String objectKey = "test-object-key.jpeg";
        String contentType = "image/jpeg";
        Long contentLength = 1024L;
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenThrow(
                        SdkException.builder()
                                .message("Put Object 용 Presigned URL 발급 실패. FileStorage 상태를 확인하세요.")
                                .build()
                );

        // when // then
        assertThatThrownBy(() -> s3Service.createPutObjectPresignedUrl(objectKey, contentType, contentLength))
                .isInstanceOf(FileStorageException.class)
                .hasMessage("Put Object 용 Presigned URL 발급 실패. FileStorage 상태를 확인하세요.");
    }

    @Test
    @DisplayName("Get Presigned URL 발급 성공 - 파일을 조회할 수 있는 Presigned Url을 발급한다.")
    void createGetObjectPresignedUrlSuccessTest() throws Exception {
        // given
        String objectKey = "test-object-key.jpeg";
        // Mock 객체 응답값 설정
        URL testUrl = new URI("https://test-bucket.s3.amazonaws.com/test-upload.jpg?presigned").toURL();
        Instant expiration = Instant.now().plus(Duration.ofMinutes(10));
        PresignedGetObjectRequest mockPresignedRequest = mock(PresignedGetObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(testUrl);
        when(mockPresignedRequest.expiration()).thenReturn(expiration);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // when
        PresignedUrl response = s3Service.createGetObjectPresignedUrl(objectKey);

        // then
        assertThat(response).isNotNull();
        assertThat(response.presignedUrl()).isEqualTo(testUrl.toString());
        assertThat(response.httpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(response.expiresAt()).isEqualTo(expiration);
        ArgumentCaptor<GetObjectPresignRequest> captor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(s3Presigner).presignGetObject(captor.capture());
        GetObjectPresignRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.signatureDuration()).isEqualTo(Duration.ofMinutes(10));
        assertThat(capturedRequest.getObjectRequest().bucket()).isEqualTo("test-bucket");
        assertThat(capturedRequest.getObjectRequest().key()).isEqualTo(objectKey);
    }

    @Test
    @DisplayName("Get Presigned URL 발급 실패 - S3 통신 중 오류가 발생하면 FileStorageException을 반환한다.")
    void createGetObjectPresignedUrlFailTest() {
        // given
        String objectKey = "test-object-key.jpeg";
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenThrow(
                        SdkException.builder()
                                .message("Get Object 용 Presigned URL 발급 실패. FileStorage 상태를 확인하세요.")
                                .build()
                );

        // when // then
        assertThatThrownBy(() -> s3Service.createGetObjectPresignedUrl(objectKey))
                .isInstanceOf(FileStorageException.class)
                .hasMessage("Get Object 용 Presigned URL 발급 실패. FileStorage 상태를 확인하세요.");
    }

    @Test
    @DisplayName("Head Object를 통한 메타데이터 조회 성공 - 파일이 존재하는 경우 메타데이터 정보를 반환한다.")
    void getObjectMetaDataSuccessWithExistObjectTest() {
        // given
        String objectKey = "test-object-key.jpeg";
        Long contentLength = 12345L;
        String contentType = "image/jpeg";
        String eTag = "test-etag";
        Instant lastModified = Instant.now();
        HeadObjectResponse mockResponse = HeadObjectResponse.builder()
                .contentLength(contentLength)
                .contentType(contentType)
                .eTag(eTag)
                .lastModified(lastModified)
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(mockResponse);

        // when
        Optional<ObjectMetadata> result = s3Service.getObjectMetaData(objectKey);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().contentLength()).isEqualTo(contentLength);
        assertThat(result.get().contentType()).isEqualTo(contentType);
        assertThat(result.get().eTag()).isEqualTo(eTag);
        assertThat(result.get().lastModified()).isEqualTo(lastModified);
        ArgumentCaptor<HeadObjectRequest> captor = ArgumentCaptor.forClass(HeadObjectRequest.class);
        verify(s3Client).headObject(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo(objectKey);
    }

    @Test
    @DisplayName("Head Object를 통한 메타데이터 조회 성공 - 파일이 존재하지 않는 경우 Empty Optional을 반환한다.")
    void getObjectMetaDataSuccessWithNotExistObjectTest() {
        // given
        String objectKey = "test-object-key.jpeg";
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.class);

        // when
        Optional<ObjectMetadata> result = s3Service.getObjectMetaData(objectKey);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Head Object를 통한 메타데이터 조회 실패 - S3 통신 중 오류가 발생하면 FileStorageException을 반환한다.")
    void getObjectMetaDataFailTest() {
        // given
        String objectKey = "test-object-key.jpeg";
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(
                        SdkException.builder()
                                .message("Head Object 요청처리 실패. FileStorage 상태를 확인하세요.")
                                .build()
                );

        // when // then
        assertThatThrownBy(() -> s3Service.getObjectMetaData(objectKey))
                .isInstanceOf(FileStorageException.class)
                .hasMessage("Head Object 요청처리 실패. FileStorage 상태를 확인하세요.");
    }

    @Test
    @DisplayName("Object 이동 성공 - sourceObjectKey -> destinationObjectKey로 복사 후 sourceObjectKey 위치는 삭제한다.")
    void moveObjectSuccessTest() {
        // given
        String sourceObjectKey = "test-source-object-key.jpeg";
        String destinationObjectKey = "test-destination-object-key.jpeg";

        // when
        s3Service.moveObject(sourceObjectKey, destinationObjectKey);

        // then
        // copyObject가 올바른 인자와 함께 호출되었는지 검증
        ArgumentCaptor<CopyObjectRequest> copyCaptor = ArgumentCaptor.forClass(CopyObjectRequest.class);
        verify(s3Client).copyObject(copyCaptor.capture());
        assertThat(copyCaptor.getValue().sourceKey()).isEqualTo(sourceObjectKey);
        assertThat(copyCaptor.getValue().destinationKey()).isEqualTo(destinationObjectKey);
        // deleteObject가 올바른 인자와 함께 호출되었는지 검증
        ArgumentCaptor<DeleteObjectRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue().key()).isEqualTo(sourceObjectKey);
    }

    @Test
    @DisplayName("Object 이동 성공 - sourceObjectKey == destinationObjectKey 인 경우 이동하지 않고 바로 종료한다.")
    void moveObjectSuccessWhenSameObjectKeyTest() {
        // given
        String sourceObjectKey = "test-source-object-key.jpeg";
        String destinationObjectKey = "test-source-object-key.jpeg";

        // when
        s3Service.moveObject(sourceObjectKey, destinationObjectKey);

        // then
        verify(s3Client, never()).copyObject(any(CopyObjectRequest.class));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Object 이동 실패 - S3 통신 중 오류가 발생하면 FileStorageException을 반환한다.")
    void moveObjectFailTest() {
        // given
        String sourceObjectKey = "test-source-object-key.jpeg";
        String destinationObjectKey = "test-destination-object-key.jpeg";
        when(s3Client.copyObject(any(CopyObjectRequest.class)))
                .thenThrow(
                        SdkException.builder()
                                .message("Object 복사 요청처리 실패. FileStorage 상태를 확인하세요.")
                                .build()
                );

        // when // then
        assertThatThrownBy(() -> s3Service.moveObject(sourceObjectKey, destinationObjectKey))
                .isInstanceOf(FileStorageException.class)
                .hasMessage("Object 이동 요청처리 실패. FileStorage 상태를 확인하세요.");
    }

}