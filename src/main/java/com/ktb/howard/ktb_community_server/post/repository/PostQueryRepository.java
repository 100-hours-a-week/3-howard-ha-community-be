package com.ktb.howard.ktb_community_server.post.repository;

import com.ktb.howard.ktb_community_server.post.dto.PostDetailWithLikeInfoDto;
import com.ktb.howard.ktb_community_server.post.dto.QPostDetailWithLikeInfoDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.ktb.howard.ktb_community_server.post.domain.QPost.post;
import static com.ktb.howard.ktb_community_server.post_like.domain.QPostLike.postLike;

@AllArgsConstructor
@Repository
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<PostDetailWithLikeInfoDto> getPostDetail(Long postId, Integer memberId) {
        PostDetailWithLikeInfoDto postDetail = queryFactory
                .select(new QPostDetailWithLikeInfoDto(
                        post.id,
                        post.title,
                        post.content,
                        post.likeCount,
                        post.viewCount,
                        post.commentCount,
                        postLike.isNotNull(),
                        post.writer.id,
                        post.writer.email,
                        post.writer.nickname,
                        post.createdAt
                ))
                .from(post)
                .leftJoin(postLike)
                    .on(post.id.eq(postLike.post.id).and(postLike.member.id.eq(memberId)))
                .where(post.id.eq(postId))
                .fetchFirst();
        return Optional.ofNullable(postDetail);
    }

}
