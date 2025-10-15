package com.ktb.howard.ktb_community_server.cache.repository;

import com.ktb.howard.ktb_community_server.post.dto.CountInfoDto;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Repository
public class LikeCountCacheRepository {

    private final Map<Long, AtomicLong> cache = new ConcurrentHashMap<>();
    private final PostRepository postRepository;

    public void safePut(Long postId, Long likeCount) {
        cache.putIfAbsent(postId, new AtomicLong(likeCount));
    }

    public void forcePut(Long postId, Long likeCount) {
        log.info("LikeCountCache 강제갱신 : postId={}, likeCount={}", postId, likeCount);
        cache.put(postId, new AtomicLong(likeCount));
    }

    @Transactional(readOnly = true)
    public Long get(Long postId) {
        return cache.computeIfAbsent(postId, c -> getCountWhenCacheMiss(postId)).longValue();
    }

    @Transactional(readOnly = true)
    public void increaseCount(Long postId) {
        log.info("LikeCountCache 카운트 증가 : postId={}", postId);
        cache.computeIfAbsent(postId, c -> getCountWhenCacheMiss(postId)).incrementAndGet();
    }

    @Transactional
    public void decreaseCount(Long postId) {
        log.info("LikeCountCache 카운트 감소 : postId={}", postId);
        cache.computeIfAbsent(postId, c -> getCountWhenCacheMiss(postId)).decrementAndGet();
    }

    public void remove(Long postId) {
        if (cache.remove(postId) != null) {
            log.info("LikeCountCache 데이터 제거 : postId={}", postId);
        }
    }

    public void clearCache() {
        log.info("LikeCountCache 초기화");
        cache.clear();
    }

    public int getCacheSize() {
        return cache.size();
    }

    public Map<Long, Long> getAllCacheDataAsMap() {
        return cache.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    private AtomicLong getCountWhenCacheMiss(Long postId) {
        Optional<CountInfoDto> postCountInfoDto = postRepository.findPostCountInfoById(postId);
        return new AtomicLong(postCountInfoDto.map(CountInfoDto::likeCount).orElse(0));
    }

}
