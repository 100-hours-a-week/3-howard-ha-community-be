package com.ktb.howard.ktb_community_server.post.repository;

import com.ktb.howard.ktb_community_server.post.domain.Post;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p where p.id <= :lastPostId order by p.createdAt desc")
    Slice<Post> findPostsNextPage(@Param("lastPostId") Long lastPostId, PageRequest pageRequest);

    @Query("select p from Post p order by p.createdAt desc")
    Slice<Post> findPosts(PageRequest pageRequest);

}
