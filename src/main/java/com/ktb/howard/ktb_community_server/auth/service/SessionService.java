package com.ktb.howard.ktb_community_server.auth.service;

import com.ktb.howard.ktb_community_server.auth.domain.MemberRole;
import com.ktb.howard.ktb_community_server.auth.domain.Session;
import com.ktb.howard.ktb_community_server.auth.repository.SessionRepository;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.exception.MemberNotFoundException;
import com.ktb.howard.ktb_community_server.member.exception.PasswordNotMatchedException;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class SessionService {

    private final MemberRepository memberRepository;
    private final SessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Session login(String email, String password) {
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
                .nickname(member.getNickname())
                .role(MemberRole.USER)
                .build();
        sessionRepository.save(session);
        return session;
    }

    public Optional<Session> findSessionAndSlide(String sessionId) {
        // 1. CrudRepository를 통해 HGETALL 실행 (세션 데이터 조회 및 유효성 확인)
        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            // 2. RedisTemplate을 통해 EXPIRE 명령어 실행 (슬라이딩)
            String redisKey = Session.KEY_PREFIX + ":" + sessionId;
            redisTemplate.expire(redisKey, 3_600, TimeUnit.SECONDS);
        }
        return sessionOpt;
    }

    // 세션 정보를 업데이트 함
    public void updateSession(Session currentSession, Member updatedMember) {
        currentSession.setNickname(updatedMember.getNickname());
        sessionRepository.save(currentSession);
    }

    // 로그아웃 수행
    public void logout(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }

    // 특정 사용자와 매칭되는 비밀번호인지 검증
    private boolean checkPassword(Member member, String rawPassword) {
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }

}
