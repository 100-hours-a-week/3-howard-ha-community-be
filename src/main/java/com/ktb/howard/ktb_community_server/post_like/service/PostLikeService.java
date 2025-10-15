package com.ktb.howard.ktb_community_server.post_like.service;

import com.ktb.howard.ktb_community_server.like_log.domain.LikeLogType;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import com.ktb.howard.ktb_community_server.post_like.domain.PostLike;
import com.ktb.howard.ktb_community_server.member.exception.MemberNotFoundException;
import com.ktb.howard.ktb_community_server.post_like.exception.InvalidLikeLogTypeException;
import com.ktb.howard.ktb_community_server.post_like.exception.PostLikeAlreadyExistException;
import com.ktb.howard.ktb_community_server.post_like.exception.PostLikeNotFoundException;
import com.ktb.howard.ktb_community_server.post.exception.PostNotFoundException;
import com.ktb.howard.ktb_community_server.post_like.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void updatePostLike(Long postId, Integer memberId, LikeLogType type) {
        Optional<PostLike> postLikeOpt = postLikeRepository.findPostLikeByPostIdAndMemberId(postId, memberId);
        if (LikeLogType.LIKE.equals(type)) {
            if (postLikeOpt.isPresent()) {
                log.error("이미 '좋아요'한 게시글 : postId={}, member={}", postId, memberId);
                throw new PostLikeAlreadyExistException("이미 해당 게시글에 좋아요 한 상태입니다.");
            }
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> {
                        log.error("존재하지 않는 게시글 : postId={}", postId);
                        return new PostNotFoundException("존재하지 않는 게시글입니다.");
                    });
            Member member = memberRepository.findById(memberId.longValue())
                    .orElseThrow(() -> {
                        log.error("존재하지 않는 회원 : memberId={}", memberId);
                        return new MemberNotFoundException("존재하지 않는 회원입니다.");
                    });
            PostLike newPostLike = PostLike.builder()
                    .member(member)
                    .post(post)
                    .build();
            postLikeRepository.save(newPostLike);
        } else if (LikeLogType.CANCEL.equals(type)) {
            PostLike deletePostLike = postLikeOpt.orElseThrow(() -> {
                log.error("취소할 좋아요 없음 : postId={}, memberId={}", postId, memberId);
                return new PostLikeNotFoundException("취소할 좋아요가 없습니다.");
            });
            postLikeRepository.delete(deletePostLike);
        } else {
            log.error("유효하지 않은 좋아요 로그 타입 : {}", type);
            throw new InvalidLikeLogTypeException("유효하지 않은 좋아요 로그 타입입니다.");
        }
    }

}
