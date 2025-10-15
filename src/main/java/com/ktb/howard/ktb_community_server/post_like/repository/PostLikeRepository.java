package com.ktb.howard.ktb_community_server.post_like.repository;

import com.ktb.howard.ktb_community_server.post.domain.Post;
import com.ktb.howard.ktb_community_server.post_like.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    @Query("select pl from PostLike pl where pl.post.id = :postId and pl.member.id = :memberId")
    Optional<PostLike> findPostLikeByPostIdAndMemberId(
            @Param("postId") Long postId,
            @Param("memberId") Integer memberId
    );

    PostLike post(Post post);
}
