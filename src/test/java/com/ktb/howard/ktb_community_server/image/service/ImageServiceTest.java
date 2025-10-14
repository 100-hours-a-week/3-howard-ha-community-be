package com.ktb.howard.ktb_community_server.image.service;

import com.ktb.howard.ktb_community_server.image.repository.ImageRepository;
import com.ktb.howard.ktb_community_server.infra.aws.s3.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ImageService imageService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(imageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(imageService, "region", "ap-northeast-2");
    }

    // TODO : 추후 관련 단위 테스트 작성 + 통합 테스트와의 분리도 필요함

}