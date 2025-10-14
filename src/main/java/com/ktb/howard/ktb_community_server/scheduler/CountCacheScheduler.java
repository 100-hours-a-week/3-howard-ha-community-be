package com.ktb.howard.ktb_community_server.scheduler;

import com.ktb.howard.ktb_community_server.cache.repository.LikeCountCacheRepository;
import com.ktb.howard.ktb_community_server.cache.repository.ViewCountCacheRepository;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class CountCacheScheduler {

    private final ViewCountCacheRepository viewCountCacheRepository;
    private final LikeCountCacheRepository likeCountCacheRepository;
    private final PostRepository postRepository;

    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void syncCountsToDatabase() {
        log.info("[스케줄러 시작] Cache에 저장된 조회수 및 좋아요 수 DB 동기화 시작");
        Map<Long, Long> viewCounts = viewCountCacheRepository.getAllCacheDataAsMap();
        Map<Long, Long> likeCounts = likeCountCacheRepository.getAllCacheDataAsMap();
        if (!viewCounts.isEmpty()) {
            postRepository.bulkUpdateViewCounts(viewCounts);
            log.info("[스케줄러] {}개의 게시글 조회수를 DB에 반영완료", viewCounts.size());
        }
        if (!likeCounts.isEmpty()) {
            postRepository.bulkUpdateLikeCounts(likeCounts);
            log.info("[스케줄러] {}개의 게시글 좋아요 수를 DB에 반영완료", likeCounts.size());
        }
        log.info("[스케줄러 종료] Cache에 저장된 모든 카운트 정보 DB 동기화 완료.");
    }

}
