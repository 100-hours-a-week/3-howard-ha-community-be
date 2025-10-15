package com.ktb.howard.ktb_community_server.like_log.service;

import com.ktb.howard.ktb_community_server.like_log.domain.LikeLog;
import com.ktb.howard.ktb_community_server.like_log.domain.LikeLogType;
import com.ktb.howard.ktb_community_server.like_log.repository.LikeLogRepository;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LikeLogService {

    private final LikeLogRepository likeLogRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public LikeLog createLikeLog(Long postId, Integer memberId, LikeLogType type) {
        Post post = postRepository.getReferenceById(postId);
        Member member = memberRepository.getReferenceById(memberId.longValue());
        LikeLog likeLog = LikeLog.builder()
                .post(post)
                .member(member)
                .type(type)
                .build();
        likeLogRepository.save(likeLog);
        return likeLog;
    }

}
