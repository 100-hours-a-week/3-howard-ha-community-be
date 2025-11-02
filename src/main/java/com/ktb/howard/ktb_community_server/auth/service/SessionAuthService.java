package com.ktb.howard.ktb_community_server.auth.service;

import com.ktb.howard.ktb_community_server.auth.domain.MemberRole;
import com.ktb.howard.ktb_community_server.auth.domain.Session;
import com.ktb.howard.ktb_community_server.auth.dto.AuthResponseDto;
import com.ktb.howard.ktb_community_server.auth.dto.SessionResponseDto;
import com.ktb.howard.ktb_community_server.auth.exception.SessionIdNotFoundInRequestException;
import com.ktb.howard.ktb_community_server.auth.exception.SessionNotFoundException;
import com.ktb.howard.ktb_community_server.auth.repository.SessionRepository;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.exception.MemberNotFoundException;
import com.ktb.howard.ktb_community_server.member.exception.PasswordNotMatchedException;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.ktb.howard.ktb_community_server.api.AuthErrorCode.SESSION_ID_NOT_FOUND_IN_REQUEST;
import static com.ktb.howard.ktb_community_server.api.AuthErrorCode.SESSION_NOT_FOUND;
import static com.ktb.howard.ktb_community_server.api.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.ktb.howard.ktb_community_server.api.MemberErrorCode.PASSWORD_NOT_MATCHED;
import static com.ktb.howard.ktb_community_server.auth.provider.CookieProvider.*;

/**
 * 세션 기반 인증 관련 로직을 처리하는 Service
 */
@Slf4j
@RequiredArgsConstructor
public class SessionAuthService implements AuthService {

    private final MemberRepository memberRepository;
    private final SessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.auth.session.session-ttl-sec}")
    private Long sessionTtlSec;

    @Transactional(readOnly = true)
    public AuthResponseDto login(String email, String password) {
        // 1. 입력한 이메일, 비밀번호 검증
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(MEMBER_NOT_FOUND));
        if (!checkPassword(member, password)) {
            throw new PasswordNotMatchedException(PASSWORD_NOT_MATCHED);
        }
        // 2. 유효한 회원인 경우 세션ID 발급
        String newSessionId = UUID.randomUUID().toString();
        // 3. 발급한 세션ID와 회원정보를 SessionStore에 저장
        Session session = Session.builder()
                .sessionId(newSessionId)
                .memberId(member.getId())
                .email(member.getEmail())
                .role(MemberRole.USER)
                .build();
        sessionRepository.save(session);
        return SessionResponseDto.builder()
                .sessionId(newSessionId)
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .role(MemberRole.USER)
                .build();
    }

    @Override
    public Optional<AuthResponseDto> refresh(String sessionId) {
        // 1. refresh 상 대상 Session 탐색
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(SESSION_NOT_FOUND));
        // 2. RedisTemplate을 통해 EXPIRE 명령어 실행 (슬라이딩)
        String redisKey = Session.KEY_PREFIX + ":" + sessionId;
        redisTemplate.expire(redisKey, sessionTtlSec, TimeUnit.SECONDS);
        // 3. refresh 한 Session 정보 구성하여 반환
        SessionResponseDto sessionResponse = SessionResponseDto.builder()
                .sessionId(session.getSessionId())
                .memberId(session.getMemberId())
                .email(session.getEmail())
                .role(session.getRole())
                .build();
        return Optional.of(sessionResponse);
    }

    // 로그아웃 수행
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = getSessionId(request)
                .orElseThrow(() -> new SessionIdNotFoundInRequestException(SESSION_ID_NOT_FOUND_IN_REQUEST));
        sessionRepository.deleteById(sessionId);
    }

    // 특정 사용자와 매칭되는 비밀번호인지 검증
    private boolean checkPassword(Member member, String rawPassword) {
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }

    // Cookie에 들어있는 Session ID를 획득
    private Optional<String> getSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(SESSION_ID_COOKIE_NAME))
                .map(Cookie::getValue)
                .findFirst();
    }

}
