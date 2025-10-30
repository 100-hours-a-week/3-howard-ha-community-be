package com.ktb.howard.ktb_community_server.config;

import com.ktb.howard.ktb_community_server.auth.repository.JwtRepository;
import com.ktb.howard.ktb_community_server.auth.repository.SessionRepository;
import com.ktb.howard.ktb_community_server.auth.service.AuthService;
import com.ktb.howard.ktb_community_server.auth.service.JwtAuthService;
import com.ktb.howard.ktb_community_server.auth.service.JwtProvider;
import com.ktb.howard.ktb_community_server.auth.service.SessionAuthService;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthConfig {

    /**
     * Session 기반 인증을 수행하는 Service Bean 생성
     */
    @Bean
    @ConditionalOnProperty(name = "app.auth.method", havingValue = "session")
    public AuthService sessionAuthService(
            MemberRepository memberRepository,
            SessionRepository sessionRepository,
            RedisTemplate<String, Object> redisTemplate,
            PasswordEncoder passwordEncoder
    ) {
        return new SessionAuthService(memberRepository, sessionRepository, redisTemplate, passwordEncoder);
    }

    /**
     * JWT 기반 인증을 수행하는 Service Bean 생성
     */
    @Bean
    @ConditionalOnProperty(name = "app.auth.method", havingValue = "jwt")
    public AuthService jwtAuthService(
            MemberRepository memberRepository,
            JwtRepository jwtRepository,
            PasswordEncoder passwordEncoder,
            RedisTemplate<String, Object> redisTemplate,
            JwtProvider jwtProvider
    ) {
        return new JwtAuthService(memberRepository, jwtRepository, passwordEncoder, redisTemplate, jwtProvider);
    }

}
