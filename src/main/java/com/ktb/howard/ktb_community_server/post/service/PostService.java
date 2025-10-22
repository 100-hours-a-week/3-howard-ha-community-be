package com.ktb.howard.ktb_community_server.post.service;

import com.google.common.base.Strings;
import com.ktb.howard.ktb_community_server.cache.repository.LikeCountCacheRepository;
import com.ktb.howard.ktb_community_server.cache.repository.ViewCountCacheRepository;
import com.ktb.howard.ktb_community_server.image.domain.Image;
import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import com.ktb.howard.ktb_community_server.image.dto.CreateImageViewUrlRequestDto;
import com.ktb.howard.ktb_community_server.image.dto.ImageUrlResponseDto;
import com.ktb.howard.ktb_community_server.image.service.ImageService;
import com.ktb.howard.ktb_community_server.like_log.domain.LikeLogType;
import com.ktb.howard.ktb_community_server.like_log.service.LikeLogService;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.dto.MemberInfoResponseDto;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.member.service.MemberService;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.dto.*;
import com.ktb.howard.ktb_community_server.post.exception.PostNotFoundException;
import com.ktb.howard.ktb_community_server.post.repository.PostQueryRepository;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import com.ktb.howard.ktb_community_server.post_like.exception.InvalidLikeLogTypeException;
import com.ktb.howard.ktb_community_server.post_like.service.PostLikeService;
import com.ktb.howard.ktb_community_server.view_log.service.ViewLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final ImageService imageService;
    private final MemberService memberService;
    private final ViewLogService viewLogService;
    private final PostRepository postRepository;
    private final PostQueryRepository postQueryRepository;
    private final PostLikeService postLikeService;
    private final MemberRepository memberRepository;
    private final LikeCountCacheRepository likeCountCacheRepository;
    private final ViewCountCacheRepository viewCountCacheRepository;
    private final LikeLogService likeLogService;

    @Transactional
    public CreatePostResponseDto createPost(
            Integer memberId,
            String title,
            String content,
            List<PostImageRequestInfoDto> postImages
    ) {
        Member writer = memberRepository.getReferenceById(memberId.longValue());
        Post post = Post.builder()
                .writer(writer)
                .title(title)
                .content(content)
                .build();
        postRepository.save(post);

        if (postImages != null && !postImages.isEmpty()) {
            postImages.forEach(i -> {
                if (!imageService.isExist(i.imageId())) {
                    log.error("이미지 {}가 존재하지 않습니다.", i.imageId());
                    throw new IllegalStateException(String.format("이미지 %d가 존재하지 않습니다.", i.imageId()));
                }
                imageService.persistImage(i.imageId(), writer, post.getId());
            });
        }

        return new CreatePostResponseDto(
                post.getId(),
                writer.getId(),
                post.getTitle(),
                post.getContent(),
                postImages
        );
    }

    @Transactional(readOnly = true)
    public List<GetPostsResponseDto> getPosts(Long cursor, Integer size) {
        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<Post> posts;
        if (cursor == 0) {
            posts = postRepository.findPosts(pageRequest);
        } else {
            posts = postRepository.findPostsNextPage(cursor, pageRequest);
        }
        return posts.stream()
                .map(p -> {
                    MemberInfoResponseDto profile = memberService
                            .getProfile(p.getWriter().getId());
                    return new GetPostsResponseDto(
                            p.getId(),
                            p.getTitle(),
                            likeCountCacheRepository.get(p.getId()).intValue(),
                            p.getCommentCount(),
                            viewCountCacheRepository.get(p.getId()),
                            p.getCreatedAt(),
                            profile
                    );
                }).toList();
    }

    @Transactional
    public PostDetailDto getPostDetail(Long postId, Integer requestMemberId) {
        PostDetailWithLikeInfoDto postDetail = postQueryRepository.getPostDetail(postId, requestMemberId)
                .orElseThrow(() -> {
                    log.error("찾을 수 없는 게시글 = {}", postId);
                    return new PostNotFoundException("존재하지 않는 게시글입니다.");
                });
        MemberInfoResponseDto profile = memberService.getProfile(postDetail.writerId());
        List<PostImageInfoDto> postImages = imageService.createImageViewUrl(new CreateImageViewUrlRequestDto(ImageType.POST, postId))
                .stream()
                .map(pi -> new PostImageInfoDto(pi.imageId(), pi.url(), pi.sequence(), pi.expiresAt()))
                .toList();
        viewCountCacheRepository.increaseCount(postId); // Cache에 조회수 갱신
        viewLogService.createViewLog(postId, requestMemberId); // 조회 이벤트에 대한 로그 추가
        return PostDetailDto.builder()
                .postId(postId)
                .writer(profile)
                .postImages(postImages)
                .title(postDetail.title())
                .content(postDetail.content())
                .likeCount(likeCountCacheRepository.get(postId).intValue())
                .viewCount(viewCountCacheRepository.get(postId))
                .commentCount(postDetail.commentCount())
                .isLiked(postDetail.isLiked())
                .createdAt(postDetail.createdAt())
                .build();
    }

    @Transactional
    public void likePost(Long postId, Integer memberId, LikeLogType type) {
        postLikeService.updatePostLike(postId, memberId, type); // 게시글 좋아요 정보 업데이트
        likeLogService.createLikeLog(postId, memberId, type);   // 게시글 좋아요 로그 추가
        // 캐시정보 갱신
        if (LikeLogType.LIKE.equals(type)) {
            likeCountCacheRepository.increaseCount(postId);
        } else if (LikeLogType.CANCEL.equals(type)) {
            likeCountCacheRepository.decreaseCount(postId);
        } else {
            log.error("유효하지 않은 좋아요 로그 타입 : {}", type);
            throw new InvalidLikeLogTypeException("유효하지 않은 좋아요 로그 타입입니다.");
        }
    }

    @Transactional
    public void updatePost(
            Integer loginMemberId,
            Long postId,
            String title,
            String content,
            List<PostImageRequestInfoDto> requestImages
    ) {
        // 1. 수정할 대상인 게시글 정보를 불러옴
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.error("수정할 게시글을 찾을 수 없음 : postId={}", postId);
            return new PostNotFoundException("수정할 게시글을 찾을 수 없습니다.");
        });
        // 2. 현재 요청자가 해당 게시글을 수정할 권한이 있는 지 확인
        if (!loginMemberId.equals(post.getWriter().getId())) {
            log.error("올바르지 않은 요청 : loginMemberId={}, postWriterId={}", loginMemberId, post.getWriter().getId());
            throw new AccessDeniedException("올바르지 않은 요청입니다.");
        }
        // 3. 제목에 대한 변경요청이 있는 경우 업데이트를 진행함
        if (!Strings.isNullOrEmpty(title)) {
            post.updateTitle(title);
        }
        // 4. 본문에 대한 변경요청이 있는 경우 업데이트를 진행함
        if (!Strings.isNullOrEmpty(content)) {
            post.updateContent(content);
        }
        // 5. 게시글 이미지에 대한 변경 요청이 있는 경우 업데이트를 진행함
        if (requestImages != null) {
            // 5-1. 기존 이미지들을 ID를 Key로 하는 Map으로 변환
            Map<Long, Image> existingImageMap = imageService.findImages(ImageType.POST, postId).stream()
                    .collect(Collectors.toMap(Image::getId, Function.identity()));

            // 3-2. 요청된 이미지 ID Set 생성 (삭제 대상 식별용)
            Set<Long> requestImageIds = requestImages.stream()
                    .map(PostImageRequestInfoDto::imageId)
                    .collect(Collectors.toSet());

            // 3-3. 삭제 대상 처리 (기존 이미지 중 요청에 없는 것)
            existingImageMap.keySet().stream()
                    .filter(existingId -> !requestImageIds.contains(existingId))
                    .forEach(imageService::deleteImage); // imageId로 soft-delete

            // 3-4. 추가 및 순서 변경 처리
            for (PostImageRequestInfoDto requestImage : requestImages) {
                Long imageId = requestImage.imageId();
                Integer newSequence = requestImage.sequence();
                Image existingImage = existingImageMap.get(imageId);
                if (existingImage != null) {
                    // 수정 대상: 순서가 변경되었다면 업데이트
                    if (!existingImage.getSequence().equals(newSequence)) {
                        existingImage.updateSequence(newSequence);
                    }
                } else {
                    // 추가 대상: 이미지를 영속화하고 게시글과 연결
                    imageService.persistImage(imageId, post.getWriter(), loginMemberId.longValue());
                }
            }
        }
    }

    @Transactional
    public void deletePostById(Integer loginMemberId, Long postId) {
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 게시글입니다."));
        if (!loginMemberId.equals(findPost.getWriter().getId())) {
            throw new AccessDeniedException("올바르지 않은 요청입니다.");
        }
        likeCountCacheRepository.remove(postId); // 좋아요 수 캐시에서 해당 post 제거
        viewCountCacheRepository.remove(postId); // 조회수 캐시에서 해당 post 제거
        postRepository.deleteById(postId);
    }

}
