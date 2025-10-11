package com.ktb.howard.ktb_community_server.image.repository;

import com.ktb.howard.ktb_community_server.image.domain.Image;
import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("select i.objectKey from Image i where i.id = :id")
    String findObjectKeyById(@Param("id") Long id);

    @Query("select i.id from Image i where i.imageType = :imageType and i.owner.id = :ownerId")
    Long findImageIdByImageTypeAndOwner(@Param("imageType") ImageType imageType, @Param("ownerId") Integer ownerId);

}
