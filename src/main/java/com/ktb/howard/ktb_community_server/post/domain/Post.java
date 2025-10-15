package com.ktb.howard.ktb_community_server.post.domain;

import com.ktb.howard.ktb_community_server.BaseEntity;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(
        name = "post",
        indexes = @Index(
                name = "idx_post_deleted_at_created_at",
                columnList = "deleted_at, created_at"
        )
)
@SQLDelete(sql = "UPDATE post SET deleted_at = NOW() WHERE post_id = ?")
@SQLRestriction("deleted_at is NULL")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member writer;

    @Column(name = "title", length = 26, nullable = false)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;

    @Builder
    public Post(Member writer, String title, String content) {
        this.writer = writer;
        this.title = title;
        this.content = content;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

}
