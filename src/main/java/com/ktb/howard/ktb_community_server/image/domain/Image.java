package com.ktb.howard.ktb_community_server.image.domain;

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
        name = "image",
        indexes = {
                @Index(
                        name = "idx_image_owner_id",
                        columnList = "owner_id"
                ),
                @Index(
                        name = "idx_image_type_reference_id",
                        columnList = "image_type, reference_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"bucket_name", "object_key"}
                )
        }
)
@SQLDelete(sql = "UPDATE image SET deleted_at = NOW() WHERE image_id = ?")
@SQLRestriction("deleted_at is NULL")
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Member owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", length = 20, nullable = false)
    private ImageType imageType;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Column(name = "region", length = 100, nullable = false)
    private String region;

    @Column(name = "object_key", length = 512, nullable = false)
    private String objectKey;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "sequence", nullable = false)
    private Integer sequence = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ImageStatus status;

    @Builder
    public Image(ImageType imageType,
                 String bucketName,
                 String region,
                 String objectKey,
                 String fileName,
                 Long fileSize,
                 String mimeType,
                 Integer sequence,
                 ImageStatus status
    ) {
        this.imageType = imageType;
        this.bucketName = bucketName;
        this.region = region;
        this.objectKey = objectKey;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.sequence = sequence;
        this.status = status;
    }

    public void updateStatus(ImageStatus status) {
        this.status = status;
    }

    public void updateOwner(Member owner) {
        this.owner = owner;
    }

    public void updateReference(Long referenceId) {
        this.referenceId = referenceId;
    }

    public void updateObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public void updateSequence(Integer sequence) {
        this.sequence = sequence;
    }

}
