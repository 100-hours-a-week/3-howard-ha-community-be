package com.ktb.howard.ktb_community_server.post.service;

import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import com.ktb.howard.ktb_community_server.image.dto.CreateImageViewUrlRequestDto;
import com.ktb.howard.ktb_community_server.image.dto.GetImageUrlResponseDto;
import com.ktb.howard.ktb_community_server.image.service.ImageService;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.dto.MemberInfoResponseDto;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.member.service.MemberService;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.dto.CreatePostResponseDto;
import com.ktb.howard.ktb_community_server.post.dto.PostDetailDto;
import com.ktb.howard.ktb_community_server.post.dto.PostImageInfoDto;
import com.ktb.howard.ktb_community_server.post.dto.PostImageRequestInfoDto;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static com.ktb.howard.ktb_community_server.image.dto.CreateImageViewUrlRequestDto.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final ImageService imageService;
    private final MemberService memberService;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

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
    public PostDetailDto getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 게시글입니다."));
        Member writer = post.getWriter();
        MemberInfoResponseDto profile = memberService.getProfile(writer.getId(), writer.getEmail(), writer.getNickname());
        List<ImageViewRequestInfoDto> request = imageService.getPostImageByPostId(postId).stream()
                .map(i -> new ImageViewRequestInfoDto(i.getImageType(), i.getId(), i.getSequence()))
                .toList();
        GetImageUrlResponseDto imageViewUrl = imageService.createImageViewUrl(new CreateImageViewUrlRequestDto(
                ImageType.POST,
                request
        ));
        List<PostImageInfoDto> postImages = imageViewUrl.getImages().stream()
                .map(pi -> new PostImageInfoDto(pi.url(), pi.sequence(), pi.expiresAt()))
                .toList();
        return PostDetailDto.builder()
                .postId(postId)
                .writer(profile)
                .postImages(postImages)
                .title(post.getTitle())
                .content(post.getContent())
                .likeCount(post.getLikeCount())
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

    @Transactional
    public void deletePostById(Integer loginMemberId, Long postId) {
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 게시글입니다."));
        if (!loginMemberId.equals(findPost.getWriter().getId())) {
            throw new AccessDeniedException("올바르지 않은 요청입니다.");
        }
        postRepository.deleteById(postId);
    }

}
