package com.ktb.howard.ktb_community_server.auth.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * Redis에 저장하여 관리하는 세션정보
 */
@Builder
@RedisHash(value = "session", timeToLive = 3600)
@Getter @Setter
@AllArgsConstructor
public class Session {

    public static final String KEY_PREFIX = "session";

    @Id
    private String sessionId;

    private Integer memberId;

    private String email;

    private MemberRole role;

}
