package com.ktb.howard.ktb_community_server.post.repository;

import com.ktb.howard.ktb_community_server.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
