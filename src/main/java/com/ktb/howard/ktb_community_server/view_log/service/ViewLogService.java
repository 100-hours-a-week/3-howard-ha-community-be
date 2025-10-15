package com.ktb.howard.ktb_community_server.view_log.service;

import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import com.ktb.howard.ktb_community_server.view_log.domain.ViewLog;
import com.ktb.howard.ktb_community_server.view_log.repository.ViewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ViewLogService {

    private final ViewLogRepository viewLogRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    public ViewLog createViewLog(Long postId, Integer memberId) {
        Post post = postRepository.getReferenceById(postId);
        Member member = memberRepository.getReferenceById(memberId.longValue());
        ViewLog viewLog = ViewLog.builder()
                .post(post)
                .member(member)
                .build();
        viewLogRepository.save(viewLog);
        return viewLog;
    }

}
