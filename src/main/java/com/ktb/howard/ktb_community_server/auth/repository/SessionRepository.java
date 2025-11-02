package com.ktb.howard.ktb_community_server.auth.repository;

import com.ktb.howard.ktb_community_server.auth.domain.Session;
import org.springframework.data.repository.CrudRepository;

public interface SessionRepository extends CrudRepository<Session, String> {

}
