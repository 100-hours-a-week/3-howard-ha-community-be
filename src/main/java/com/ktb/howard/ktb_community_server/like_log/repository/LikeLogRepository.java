package com.ktb.howard.ktb_community_server.like_log.repository;

import com.ktb.howard.ktb_community_server.like_log.domain.LikeLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeLogRepository extends JpaRepository<LikeLog, Long> {

}
