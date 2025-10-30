package com.ktb.howard.ktb_community_server.auth.service;

import com.ktb.howard.ktb_community_server.auth.domain.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 생성, 파싱과 같은 역할을 담당하는 제공자 역할
 */
@Component
public class JwtProvider {

    private final Key key;
    private final long accessTokenTtlSec;
    private final long refreshTokenTtlSec;

    // 알고리즘 적용 시 사용할 시크릿 키 초기화
    public JwtProvider(
            @Value("${app.auth.jwt.secret-key}") String secretKey,
            @Value("${app.auth.jwt.access-token-ttl-sec}") long accessTokenTtlSec,
            @Value("${app.auth.jwt.refresh-token-ttl-sec}") long refreshTokenTtlSec
    ) {
        key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
        this.accessTokenTtlSec = accessTokenTtlSec;
        this.refreshTokenTtlSec = refreshTokenTtlSec;
    }

    // Access Token 발급
    public String createAccessToken(String email, Integer memberId, MemberRole role) {
        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusSeconds(accessTokenTtlSec)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 발급
    public String createRefreshToken(Integer memberId) {
        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .claim("typ", "refresh")
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusSeconds(refreshTokenTtlSec)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 검증 및 파싱
    public Jws<Claims> parse(String jwt) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt);
    }

}
