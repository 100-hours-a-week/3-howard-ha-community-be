package com.ktb.howard.ktb_community_server.auth.service;

import com.ktb.howard.ktb_community_server.auth.domain.MemberRole;
import com.ktb.howard.ktb_community_server.auth.domain.RefreshToken;
import com.ktb.howard.ktb_community_server.auth.dto.AuthResponseDto;
import com.ktb.howard.ktb_community_server.auth.dto.JwtResponseDto;
import com.ktb.howard.ktb_community_server.auth.repository.JwtRepository;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.exception.MemberNotFoundException;
import com.ktb.howard.ktb_community_server.member.exception.PasswordNotMatchedException;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

import static com.ktb.howard.ktb_community_server.filter.JwtAuthFilter.REFRESH_TOKEN_COOKIE_NAME;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthService implements AuthService {

    private final MemberRepository memberRepository;
    private final JwtRepository jwtRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProvider jwtProvider;
    @Value("${app.auth.cookie.secure:true}")
    private boolean isCookieSecure;

    @Override
    public AuthResponseDto login(String email, String password) {
        // 1. 유효한 회원인지 확인
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("입력한 이메일 또는 비밀번호를 확인해주세요."));
        if (!checkPassword(member, password)) {
            log.info("비밀번호 불일치");
            throw new PasswordNotMatchedException("입력한 이메일 또는 비밀번호를 확인해주세요.");
        }
        // 2. 기존 refresh token이 존재하는 경우 무효화
        jwtRepository.deleteByMemberId(member.getId());

        // 3. 새로운 access / refresh token 생성 및 반환
        String accessToken = jwtProvider.createAccessToken(email, member.getId(), MemberRole.USER);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(jwtProvider.createRefreshToken(member.getId()))
                .memberId(member.getId())
                .email(member.getEmail())
                .role(MemberRole.USER)
                .build();
        jwtRepository.save(refreshToken);
        return JwtResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .role(MemberRole.USER)
                .build();
    }

    @Override
    public Optional<AuthResponseDto> refresh(String refreshToken) {
        Jws<Claims> parsedRefreshToken = jwtProvider.parse(refreshToken);
        Optional<RefreshToken> refreshTokenOpt = jwtRepository.findByToken(refreshToken);
        if (refreshTokenOpt.isPresent()) {
            Integer memberId = Integer.parseInt(parsedRefreshToken.getBody().getSubject());
            Member member = memberRepository.findById(memberId.longValue())
                    .orElseThrow(() -> new MemberNotFoundException("확인할 수 없는 회원입니다."));
            String newAccessToken = jwtProvider.createAccessToken(member.getEmail(), member.getId(), MemberRole.USER);
            return Optional.of(
                    JwtResponseDto.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(refreshToken)
                            .memberId(member.getId())
                            .email(member.getEmail())
                            .nickname(member.getNickname())
                            .role(MemberRole.USER)
                            .build()
            );
        }
        return Optional.empty();
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // refresh token 무효화
        Optional<String> refreshTokenOpt = getRefreshToken(request);
        if (refreshTokenOpt.isEmpty()) return;
        String refreshToken = refreshTokenOpt.get();
        jwtRepository.deleteByToken(refreshToken);
    }

    // 특정 사용자와 매칭되는 비밀번호인지 검증
    private boolean checkPassword(Member member, String rawPassword) {
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }

    private Optional<String> getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(REFRESH_TOKEN_COOKIE_NAME))
                .map(Cookie::getValue) // 쿠키의 값(RefreshToken)을 추출
                .findFirst();
    }

}
