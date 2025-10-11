package com.ktb.howard.ktb_community_server.comment.repository;

import com.ktb.howard.ktb_community_server.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
