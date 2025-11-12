package com.ktb.howard.ktb_community_server.comment.repository;

import com.ktb.howard.ktb_community_server.comment.dto.GetCommentsDto;
import com.ktb.howard.ktb_community_server.comment.dto.QGetCommentsDto;
import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ktb.howard.ktb_community_server.comment.domain.QComment.comment;
import static com.ktb.howard.ktb_community_server.image.domain.QImage.image;
import static com.ktb.howard.ktb_community_server.member.domain.QMember.member;
import static com.ktb.howard.ktb_community_server.post.domain.QPost.post;

@RequiredArgsConstructor
@Repository
public class CommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Slice<GetCommentsDto> findCommentsNextPage(Long postId, Long lastComemntId, PageRequest pageRequest) {
        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.and(comment.post.id.eq(postId)).and(comment.parentComment.isNull());
        if (lastComemntId != null && lastComemntId > 0) {
            whereClause.and(comment.id.lt(lastComemntId));
        }
        List<GetCommentsDto> posts = queryFactory
                .select(new QGetCommentsDto(
                        comment.id,
                        comment.content,
                        member.id,
                        member.email,
                        member.nickname,
                        image.id,
                        image.objectKey,
                        image.sequence,
                        comment.createdAt,
                        comment.deletedAt
                ))
                .from(comment)
                .leftJoin(member).on(comment.member.id.eq(member.id))
                .leftJoin(image).on(member.id.eq(image.owner.id).and(image.imageType.eq(ImageType.PROFILE)))
                .where(whereClause)
                .orderBy(comment.createdAt.desc())
                .limit(pageRequest.getPageSize() + 1)
                .fetch();
        boolean hasNext = false;
        if (posts.size() > pageRequest.getPageSize()) {
            posts.remove(pageRequest.getPageSize());
            hasNext = true;
        }
        return new SliceImpl<>(posts, pageRequest, hasNext);
    }

    public List<GetCommentsDto> getChildComments(Long parentCommentId) {
        return queryFactory
                .select(new QGetCommentsDto(
                        comment.id,
                        comment.content,
                        member.id,
                        member.email,
                        member.nickname,
                        image.id,
                        image.objectKey,
                        image.sequence,
                        comment.createdAt,
                        comment.deletedAt
                ))
                .from(comment)
                .leftJoin(member).on(comment.member.id.eq(member.id))
                .leftJoin(image).on(member.id.eq(image.owner.id).and(image.imageType.eq(ImageType.PROFILE)))
                .where(comment.parentComment.id.eq(parentCommentId))
                .orderBy(comment.createdAt.desc())
                .fetch();
    }

}
