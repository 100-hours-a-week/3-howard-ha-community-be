package com.ktb.howard.ktb_community_server.post_image.domain;

import com.ktb.howard.ktb_community_server.BaseEntity;
import com.ktb.howard.ktb_community_server.post.domain.Post;
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
        name = "post_image",
        indexes = @Index(
                name = "idx_pi_post_id_deleted_at_sequence",
                columnList = "post_id, deleted_at, sequence"
        )
)
@SQLDelete(sql = "UPDATE post_image SET deleted_at = NOW() WHERE post_image_id = ?")
@SQLRestriction("deleted_at is NULL")
public class PostImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "sequence")
    private Integer sequence = 1;

    @Builder
    public PostImage(Post post, String imageUrl, Integer sequence) {
        this.post = post;
        this.imageUrl = imageUrl;
        this.sequence = sequence;
    }

}
