package com.ktb.howard.ktb_community_server.auth.repository;

import com.ktb.howard.ktb_community_server.auth.domain.MemberSession;
import org.springframework.data.repository.CrudRepository;

public interface MemberSessionRepository extends CrudRepository<MemberSession, String> {

}
