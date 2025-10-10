package com.ktb.howard.ktb_community_server.image.repository;

import com.ktb.howard.ktb_community_server.image.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("select i.objectKey from Image i where i.id = :id")
    String findObjectKeyById(@Param("id") Long id);

}
