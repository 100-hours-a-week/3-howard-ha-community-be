package com.ktb.howard.ktb_community_server.comment.repository;

import com.ktb.howard.ktb_community_server.comment.domain.Comment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c " +
            "from Comment c " +
            "where c.parentComment.id = :parentCommentId " +
            "order by c.createdAt desc")
    List<Comment> findByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    @Query("select c " +
            "from Comment c " +
            "where c.post.id = :postId " +
            "order by c.createdAt desc")
    Slice<Comment> findComments(@Param("postId") Long postId, PageRequest pageRequest);

    @Query("select c " +
            "from Comment c " +
            "where c.post.id = :postId and c.id < :lastCommentId and c.parentComment is null " +
            "order by c.createdAt desc")
    Slice<Comment> findCommentsNextPage(
            @Param("postId") Long postId,
            @Param("lastCommentId") Long commentId,
            PageRequest pageRequest
    );

}
