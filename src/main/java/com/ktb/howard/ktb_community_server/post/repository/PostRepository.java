package com.ktb.howard.ktb_community_server.post.repository;

import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post.dto.CountInfoDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p where p.id < :lastPostId order by p.createdAt desc")
    Slice<Post> findPostsNextPage(@Param("lastPostId") Long lastPostId, PageRequest pageRequest);

    @Query("select p from Post p order by p.createdAt desc")
    Slice<Post> findPosts(PageRequest pageRequest);

    @Query("select new com.ktb.howard.ktb_community_server.post.dto.CountInfoDto(p.likeCount, p.viewCount) " +
            "from Post p " +
            "where p.id = :postId")
    Optional<CountInfoDto> findPostCountInfoById(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("update Post p set p.viewCount = :count where p.id = :postId")
    void updateViewCount(@Param("postId") Long postId, @Param("count") Long count);

    @Modifying(clearAutomatically = true)
    @Query("update Post p set p.likeCount = :count where p.id = :postId")
    void updateLikeCount(@Param("postId") Long postId, @Param("count") Long count);

    default void bulkUpdateViewCounts(Map<Long, Long> viewCounts) {
        for (Map.Entry<Long, Long> entry : viewCounts.entrySet()) {
            updateViewCount(entry.getKey(), entry.getValue());
        }
    }

    default void bulkUpdateLikeCounts(Map<Long, Long> likeCounts) {
        for (Map.Entry<Long, Long> entry : likeCounts.entrySet()) {
            updateLikeCount(entry.getKey(), entry.getValue());
        }
    }

}
