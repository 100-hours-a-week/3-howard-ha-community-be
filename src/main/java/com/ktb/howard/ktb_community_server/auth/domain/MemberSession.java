package com.ktb.howard.ktb_community_server.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;

@RedisHash(value = "member", timeToLive = 3600)
@Getter
@AllArgsConstructor
public class MemberSession {

    @Id
    private String sessionId;

    private String email;

    private String nickname;

    private MemberRole role;

    private Instant expiresAt;

}
