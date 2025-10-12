package com.ktb.howard.ktb_community_server.post.service;

import com.ktb.howard.ktb_community_server.image.domain.Image;
import com.ktb.howard.ktb_community_server.image.domain.ImageStatus;
import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import com.ktb.howard.ktb_community_server.image.repository.ImageRepository;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.dto.CreatePostResponseDto;
import com.ktb.howard.ktb_community_server.post.dto.PostDetailDto;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class PostServiceTest {

    @Autowired
    PostService postService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ImageRepository imageRepository;

    @Test
    @DisplayName("게시글 생성 - 게시글 이미지를 첨부하지 않은 경우")
    void createPostWithNoPostImageTest() {
        // given
        Member writer = Member.builder()
                .email("test@example.com")
                .password("Password12345!")
                .nickname("test.park")
                .build();
        memberRepository.save(writer);

        // when
        CreatePostResponseDto response = postService.createPost(
                writer.getId(),
                "테스트 제목1",
                "테스트용 게시글 본문1",
                null
        );

        // then
        Post createdPost = postRepository.findById(response.postId())
                        .orElseThrow(() -> new AssertionError("게시글이 생성되지 않았습니다."));
        assertThat(createdPost)
                .extracting("id", "writer.id", "title", "content", "likeCount", "viewCount", "commentCount")
                .containsExactly(
                        response.postId(),
                        response.writerId(),
                        response.title(),
                        response.content(),
                        0,
                        0L,
                        0L
                );
    }

    @Test
    @DisplayName("게시글 세부정보 조회 - 특정 ID를 가진 게시글의 세부정보를 조회한다.")
    void getPostDetailSuccessTest() {
        // given
        Member writer = Member.builder()
                .email("test@example.com")
                .password("Password12345!")
                .nickname("test.park")
                .build();
        memberRepository.save(writer);
        Post post = Post.builder()
                .writer(writer)
                .title("테스트 제목1")
                .content("테스트용 게시글 본문1")
                .build();
        postRepository.save(post);
        Image image1 = Image.builder()
                .imageType(ImageType.POST)
                .bucketName("test-bucket")
                .region("test-region")
                .objectKey("/test/test1.jpeg")
                .fileName("test1.jpeg")
                .fileSize(1234)
                .mimeType("image/jpeg")
                .sequence(1)
                .status(ImageStatus.PERSIST)
                .build();
        image1.updateOwner(writer);
        image1.updateReference(post.getId());
        Image image2 = Image.builder()
                .imageType(ImageType.POST)
                .bucketName("test-bucket")
                .region("test-region")
                .objectKey("/test/test2.jpeg")
                .fileName("test2.jpeg")
                .fileSize(1234)
                .mimeType("image/jpeg")
                .sequence(2)
                .status(ImageStatus.PERSIST)
                .build();
        image2.updateOwner(writer);
        image2.updateReference(post.getId());
        imageRepository.save(image1);
        imageRepository.save(image2);

        // when
        PostDetailDto response = postService.getPostDetail(post.getId());
        System.out.println("response = " + response);

        // then
        assertThat(response.getPostId()).isEqualTo(post.getId());
        assertThat(response.getTitle()).isEqualTo(post.getTitle());
        assertThat(response.getContent()).isEqualTo(post.getContent());
        assertThat(response.getLikeCount()).isZero();
        assertThat(response.getViewCount()).isZero();
        assertThat(response.getCommentCount()).isZero();
        assertThat(response.getCreatedAt()).isEqualTo(post.getCreatedAt());
        assertThat(response.getWriter())
                .extracting("email", "nickname", "profileImageUrl")
                .containsExactly(writer.getEmail(), writer.getNickname(), null);
        assertThat(response.getPostImages()).hasSize(2);
    }

    @Test
    @DisplayName("게시글 세부정보 조회 - 특정 ID를 가진 게시글이 존재하지 않는 경우, 예외를 반환한다.")
    void getPostDetailFailTest() {
        // given

        // when // then
        assertThatThrownBy(() -> postService.getPostDetail(1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 게시글입니다.");
    }

    @Test
    @DisplayName("게시글 삭제 성공 - 요청한 게시글에 대한 권한을 가진 사용자가 삭제를 요청한 경우, 대응하는 ID의 게시글을 제거한다.")
    void deletePostSuccessTest() {
        // given
        Member writer = Member.builder()
                .email("test@example.com")
                .password("Password12345!")
                .nickname("test.park")
                .build();
        memberRepository.save(writer);
        Post post = Post.builder()
                .writer(writer)
                .title("테스트 제목1")
                .content("테스트용 게시글 본문1")
                .build();
        postRepository.save(post);

        // when
        postService.deletePostById(writer.getId(), post.getId());

        // then
        Optional<Post> deletedPost = postRepository.findById(post.getId());
        assertThat(deletedPost).isNotPresent();
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 요청한 게시글에 대한 권한이 없는 사용자가 삭제를 요청한 경우, 예외를 반환한다.")
    void deletePostFailWhenNotAuthorizedTest() {
        // given
        Member writer = Member.builder()
                .email("test@example.com")
                .password("Password12345!")
                .nickname("test.park")
                .build();
        memberRepository.save(writer);
        Post post = Post.builder()
                .writer(writer)
                .title("테스트 제목1")
                .content("테스트용 게시글 본문1")
                .build();
        postRepository.save(post);

        // when // then
        assertThatThrownBy(() -> postService.deletePostById(writer.getId() + 1, post.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("올바르지 않은 요청입니다.");
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 요청한 게시글이 존재하지 않는 경우, 예외를 반환한다.")
    void deletePostFailWhenPostNotExistTest() {
        // given
        Member writer = Member.builder()
                .email("test@example.com")
                .password("Password12345!")
                .nickname("test.park")
                .build();
        memberRepository.save(writer);

        // when // then
        assertThatThrownBy(() -> postService.deletePostById(writer.getId(), 1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 게시글입니다.");
    }

}