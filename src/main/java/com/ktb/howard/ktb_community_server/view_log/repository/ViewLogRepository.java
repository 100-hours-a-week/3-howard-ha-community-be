package com.ktb.howard.ktb_community_server.view_log.repository;

import com.ktb.howard.ktb_community_server.view_log.domain.ViewLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewLogRepository extends JpaRepository<ViewLog, Long> {

}
