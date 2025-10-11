package com.ktb.howard.ktb_community_server.post.service;

import com.ktb.howard.ktb_community_server.image.service.ImageService;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.dto.CreatePostResponseDto;
import com.ktb.howard.ktb_community_server.post.dto.PostImageInfo;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final ImageService imageService;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CreatePostResponseDto createPost(
            Integer memberId,
            String title,
            String content,
            List<PostImageInfo> postImages
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

}
