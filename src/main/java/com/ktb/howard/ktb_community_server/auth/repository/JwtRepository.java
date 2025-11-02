package com.ktb.howard.ktb_community_server.auth.repository;

import com.ktb.howard.ktb_community_server.auth.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface JwtRepository extends CrudRepository<RefreshToken, Integer> {

    void deleteByMemberId(Integer memberId);

    void deleteByToken(String token);

    Optional<RefreshToken> findByToken(String token);

}
