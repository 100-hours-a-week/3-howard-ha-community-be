package com.ktb.howard.ktb_community_server.auth.service;

import com.ktb.howard.ktb_community_server.auth.domain.MemberRole;
import com.ktb.howard.ktb_community_server.auth.domain.Session;
import com.ktb.howard.ktb_community_server.auth.dto.AuthResponseDto;
import com.ktb.howard.ktb_community_server.auth.dto.SessionResponseDto;
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

import static com.ktb.howard.ktb_community_server.auth.service.CookieProvider.*;

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
        // 1. 유효한 회원인지 확인
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("입력한 이메일 또는 비밀번호를 확인해주세요."));
        if (!checkPassword(member, password)) {
            log.info("비밀번호 불일치");
            throw new PasswordNotMatchedException("입력한 이메일 또는 비밀번호를 확인해주세요.");
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
        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            // 2. RedisTemplate을 통해 EXPIRE 명령어 실행 (슬라이딩)
            String redisKey = Session.KEY_PREFIX + ":" + sessionId;
            redisTemplate.expire(redisKey, sessionTtlSec, TimeUnit.SECONDS);
            Session session = sessionOpt.get();
            return Optional.of(
                    SessionResponseDto.builder()
                            .sessionId(session.getSessionId())
                            .memberId(session.getMemberId())
                            .email(session.getEmail())
                            .role(session.getRole())
                            .build()
            );
        }
        return Optional.empty();
    }

    // 로그아웃 수행
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> sessionIdOpt = getSessionId(request);
        if (sessionIdOpt.isEmpty()) return;
        String sessionId = sessionIdOpt.get();
        sessionRepository.deleteById(sessionId);
    }

    // 특정 사용자와 매칭되는 비밀번호인지 검증
    private boolean checkPassword(Member member, String rawPassword) {
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }

    private Optional<String> getSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(SESSION_ID_COOKIE_NAME))
                .map(Cookie::getValue) // 쿠키의 값(세션 ID)을 추출
                .findFirst();
    }

}
