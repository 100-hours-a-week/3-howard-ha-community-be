package com.ktb.howard.ktb_community_server.post.service;

import com.ktb.howard.ktb_community_server.cache.repository.LikeCountCacheRepository;
import com.ktb.howard.ktb_community_server.cache.repository.ViewCountCacheRepository;
import com.ktb.howard.ktb_community_server.image.domain.Image;
import com.ktb.howard.ktb_community_server.image.domain.ImageStatus;
import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import com.ktb.howard.ktb_community_server.image.repository.ImageRepository;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.dto.CreatePostResponseDto;
import com.ktb.howard.ktb_community_server.post.dto.GetPostsResponseDto;
import com.ktb.howard.ktb_community_server.post.dto.PostDetailDto;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    @Autowired
    LikeCountCacheRepository likeCountCacheRepository;

    @Autowired
    ViewCountCacheRepository viewCountCacheRepository;

    @BeforeEach
    void init() {
        // 좋아요, 조회 수 정보 캐시 초기화
        likeCountCacheRepository.clearCache();
        viewCountCacheRepository.clearCache();
    }

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
    @DisplayName("게시글 목록 조회 - 커서의 값이 0인 경우, 가장 최근 게시글을 조회하여 반환한다.")
    void getPostsNoCursorTest() throws InterruptedException {
        // given
        Member writer = Member.builder()
                .email("test@example.com")
                .password("Password12345!")
                .nickname("test.park")
                .build();
        memberRepository.save(writer);
        Post post1 = Post.builder()
                .writer(writer)
                .title("테스트 제목1")
                .content("테스트용 게시글 본문1")
                .build();
        postRepository.save(post1);
        Thread.sleep(500);
        Post post2 = Post.builder()
                .writer(writer)
                .title("테스트 제목2")
                .content("테스트용 게시글 본문2")
                .build();
        postRepository.save(post2);
        Thread.sleep(500);
        Post post3 = Post.builder()
                .writer(writer)
                .title("테스트 제목3")
                .content("테스트용 게시글 본문3")
                .build();
        postRepository.save(post3);
        Thread.sleep(500);
        Post post4 = Post.builder()
                .writer(writer)
                .title("테스트 제목4")
                .content("테스트용 게시글 본문4")
                .build();
        postRepository.save(post4);
        Thread.sleep(500);
        Post post5 = Post.builder()
                .writer(writer)
                .title("테스트 제목5")
                .content("테스트용 게시글 본문5")
                .build();
        post5.updateDeletedAt(LocalDateTime.now());
        postRepository.save(post5);

        // when
        List<GetPostsResponseDto> posts = postService.getPosts(0L, 3);

        // then
        assertThat(posts).hasSize(3)
                .extracting(
                        "title",
                        "likeCount",
                        "viewCount",
                        "commentCount",
                        "createdAt",
                        "writer.email",
                        "writer.nickname"
                )
                .containsExactly(
                        tuple(
                                post4.getTitle(),
                                post4.getLikeCount(),
                                post4.getViewCount(),
                                post4.getCommentCount(),
                                post4.getCreatedAt(),
                                post4.getWriter().getEmail(),
                                post4.getWriter().getNickname()
                        ),
                        tuple(
                                post3.getTitle(),
                                post3.getLikeCount(),
                                post3.getViewCount(),
                                post3.getCommentCount(),
                                post3.getCreatedAt(),
                                post3.getWriter().getEmail(),
                                post3.getWriter().getNickname()
                        ),
                        tuple(
                                post2.getTitle(),
                                post2.getLikeCount(),
                                post2.getViewCount(),
                                post2.getCommentCount(),
                                post2.getCreatedAt(),
                                post2.getWriter().getEmail(),
                                post2.getWriter().getNickname()
                        )
                );
    }

    @Test
    @DisplayName("게시글 목록 조회 - 커서의 값이 0이 아닌 경우, 해당 커서 이후의 값을 조회하여 반환한다.")
    void getPostsWithCursorTest() throws InterruptedException {
        // given
        Member writer = Member.builder()
                .email("test@example.com")
                .password("Password12345!")
                .nickname("test.park")
                .build();
        memberRepository.save(writer);
        Post post1 = Post.builder()
                .writer(writer)
                .title("테스트 제목1")
                .content("테스트용 게시글 본문1")
                .build();
        postRepository.save(post1);
        Thread.sleep(500);
        Post post2 = Post.builder()
                .writer(writer)
                .title("테스트 제목2")
                .content("테스트용 게시글 본문2")
                .build();
        postRepository.save(post2);
        Thread.sleep(500);
        Post post3 = Post.builder()
                .writer(writer)
                .title("테스트 제목3")
                .content("테스트용 게시글 본문3")
                .build();
        postRepository.save(post3);
        Thread.sleep(500);
        Post post4 = Post.builder()
                .writer(writer)
                .title("테스트 제목4")
                .content("테스트용 게시글 본문4")
                .build();
        postRepository.save(post4);
        Thread.sleep(500);
        Post post5 = Post.builder()
                .writer(writer)
                .title("테스트 제목5")
                .content("테스트용 게시글 본문5")
                .build();
        post5.updateDeletedAt(LocalDateTime.now());
        postRepository.save(post5);

        // when
        List<GetPostsResponseDto> posts = postService.getPosts(post2.getId(), 3);

        // then
        assertThat(posts).hasSize(2)
                .extracting(
                        "title",
                        "likeCount",
                        "viewCount",
                        "commentCount",
                        "createdAt",
                        "writer.email",
                        "writer.nickname"
                )
                .containsExactly(
                        tuple(
                                post2.getTitle(),
                                post2.getLikeCount(),
                                post2.getViewCount(),
                                post2.getCommentCount(),
                                post2.getCreatedAt(),
                                post2.getWriter().getEmail(),
                                post2.getWriter().getNickname()
                        ),
                        tuple(
                                post1.getTitle(),
                                post1.getLikeCount(),
                                post1.getViewCount(),
                                post1.getCommentCount(),
                                post1.getCreatedAt(),
                                post1.getWriter().getEmail(),
                                post1.getWriter().getNickname()
                        )
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
                .fileSize(1234L)
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
                .fileSize(1234L)
                .mimeType("image/jpeg")
                .sequence(2)
                .status(ImageStatus.PERSIST)
                .build();
        image2.updateOwner(writer);
        image2.updateReference(post.getId());
        imageRepository.save(image1);
        imageRepository.save(image2);

        // when
        PostDetailDto response = postService.getPostDetail(post.getId(), writer.getId());

        // then
        assertThat(response.getPostId()).isEqualTo(post.getId());
        assertThat(response.getTitle()).isEqualTo(post.getTitle());
        assertThat(response.getContent()).isEqualTo(post.getContent());
        assertThat(response.getLikeCount()).isZero();
        assertThat(response.getViewCount()).isOne();
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
        assertThatThrownBy(() -> postService.getPostDetail(1L, 1))
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