package com.ktb.howard.ktb_community_server.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

/**
 * Redis에 저장하여 관리하는 세션정보
 */
@Builder
@RedisHash(value = "refresh-token")
@Getter
@Setter
@AllArgsConstructor
public class RefreshToken {

    public static final String KEY_PREFIX = "refresh-token";

    @Id
    private Integer memberId;

    @Indexed
    private String token;

    private String email;

    private MemberRole role;

    @TimeToLive
    private Long ttl;

}
