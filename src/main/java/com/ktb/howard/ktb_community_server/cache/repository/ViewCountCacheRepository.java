package com.ktb.howard.ktb_community_server.cache.repository;

import com.ktb.howard.ktb_community_server.post.dto.CountInfoDto;
import com.ktb.howard.ktb_community_server.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Repository
public class ViewCountCacheRepository {

    private final Map<Long, AtomicLong> cache = new ConcurrentHashMap<>();
    private final PostRepository postRepository;

    public void safePut(Long postId, Long viewCount) {
        cache.putIfAbsent(postId, new AtomicLong(viewCount));
    }

    public void forcePut(Long postId, Long viewCount) {
        log.info("ViewCountCache 강제갱신 : postId={}, viewCount={}", postId, viewCount);
        cache.put(postId, new AtomicLong(viewCount));
    }

    public Long get(Long postId) {
        return cache.computeIfAbsent(postId, c -> getCountWhenCacheMiss(postId)).longValue();
    }

    public void increaseCount(Long postId) {
        log.info("ViewCountCache 카운트 증가 : postId={}", postId);
        cache.computeIfAbsent(postId, c -> getCountWhenCacheMiss(postId)).incrementAndGet();
    }

    public void remove(Long postId) {
        if (cache.remove(postId) != null) {
            log.info("ViewCountCache 데이터 제거 : postId={}", postId);
        }
    }

    public void clearCache() {
        log.info("ViewCountCache 초기화");
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
        return new AtomicLong(postCountInfoDto.map(CountInfoDto::viewCount).orElse(0L));
    }

}
