package com.ktb.howard.ktb_community_server.comment.repository;

import com.ktb.howard.ktb_community_server.comment.domain.Comment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ktb.howard.ktb_community_server.comment.domain.QComment.comment;
import static com.ktb.howard.ktb_community_server.member.domain.QMember.member;

@RequiredArgsConstructor
@Repository
public class CommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Comment> findCommentsByPostId(Long postId, Pageable pageable) {
        // 데이터 목록 조회 쿼리
        List<Comment> comments = queryFactory
                .selectFrom(comment)
                .join(comment.member, member).fetchJoin()
                .where(
                        comment.post.id.eq(postId),     // 특정 게시글에 포함된 댓글조회
                        comment.parentComment.isNull(), // 대댓글은 목록조회대상X
                        comment.deletedAt.isNull()      // 삭제되지 않은 댓글
                )
                .orderBy(comment.createdAt.desc()) // 생성일 기준 내림차순 정렬
                .offset(pageable.getOffset())     // 페이지네이션 offset
                .limit(pageable.getPageSize())    // 페이지네이션 limit
                .fetch();

        // 전체 카운트 조회 쿼리 (페이지네이션을 위해 필요)
        Long totalCount = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.post.id.eq(postId),
                        comment.parentComment.isNull(),
                        comment.deletedAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(comments, pageable, totalCount != null ? totalCount : 0L);
    }

}
