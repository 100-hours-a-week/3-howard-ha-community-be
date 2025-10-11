package com.ktb.howard.ktb_community_server.comment.repository;

import com.ktb.howard.ktb_community_server.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c where c.parentComment.id = :parentCommentId")
    List<Comment> findByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Comment c set c.deletedAt = NOW() where c.id = :commentId")
    void softDeleteByCommentId(@Param("commentId") Long commentId);

}
