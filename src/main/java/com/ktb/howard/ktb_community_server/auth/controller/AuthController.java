package com.ktb.howard.ktb_community_server.auth.controller;

import com.ktb.howard.ktb_community_server.auth.dto.*;
import com.ktb.howard.ktb_community_server.auth.exception.AuthArgumentNotFoundException;
import com.ktb.howard.ktb_community_server.auth.exception.InvalidAuthResponseTypeException;
import com.ktb.howard.ktb_community_server.auth.exception.RefreshTokenNotFoundException;
import com.ktb.howard.ktb_community_server.auth.exception.SessionNotFoundException;
import com.ktb.howard.ktb_community_server.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

import static com.ktb.howard.ktb_community_server.filter.JwtAuthFilter.ACCESS_TOKEN_COOKIE_NAME;
import static com.ktb.howard.ktb_community_server.filter.JwtAuthFilter.REFRESH_TOKEN_COOKIE_NAME;
import static com.ktb.howard.ktb_community_server.filter.SessionAuthFilter.SESSION_COOKIE_NAME;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @Value("${app.auth.session.session-ttl-sec}")
    private Long sessionTTlSec;
    @Value("${app.auth.jwt.access-token-ttl-sec}")
    private Long accessTokenTtlSec;
    @Value("${app.auth.jwt.refresh-token-ttl-sec}")
    private Long refreshTokenTTlSec;
    @Value("${app.auth.cookie.secure:true}")
    private boolean isCookieSecure;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest, HttpServletResponse response) {
        AuthResponseDto authResponseDto;
        try {
            authResponseDto = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
            if (authResponseDto instanceof SessionResponseDto sessionResponseDto) {
                ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE_NAME, sessionResponseDto.getSessionId())
                        .httpOnly(true)
                        .secure(isCookieSecure)
                        .path("/")
                        .maxAge(sessionTTlSec)
                        .sameSite("None")
                        .build();
                response.addHeader("Set-Cookie", cookie.toString());
                URI location = URI.create("/auth/me");
                LoginResponseDto loginResponse = LoginResponseDto.builder()
                        .member(
                                LoginMemberInfoDto.builder()
                                        .id(authResponseDto.getMemberId())
                                        .email(authResponseDto.getEmail())
                                        .nickname(authResponseDto.getNickname())
                                        .build()
                        )
                        .message("로그인 되었습니다.")
                        .build();
                return ResponseEntity.created(location).body(loginResponse);
            } else if (authResponseDto instanceof JwtResponseDto jwtResponseDto) {
                // 1. access-token에 대한 저장을 지시하는 쿠키 구성
                ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, jwtResponseDto.getAccessToken())
                        .httpOnly(true)
                        .secure(isCookieSecure)
                        .path("/")
                        .maxAge(accessTokenTtlSec)
                        .sameSite("None")
                        .build();
                // 2. refresh-token에 대한 저장을 지시하는 쿠키 구성
                ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, jwtResponseDto.getRefreshToken())
                        .httpOnly(true)
                        .secure(isCookieSecure)
                        .path("/auth/refresh")
                        .maxAge(refreshTokenTTlSec)
                        .sameSite("None")
                        .build();
                response.addHeader("Set-Cookie", accessTokenCookie.toString());
                response.addHeader("Set-Cookie", refreshTokenCookie.toString());
                URI location = URI.create("/auth/me");
                JwtLoginResponseDto loginResponse = JwtLoginResponseDto.builder()
                        .member(
                                LoginMemberInfoDto.builder()
                                        .id(authResponseDto.getMemberId())
                                        .email(authResponseDto.getEmail())
                                        .nickname(authResponseDto.getNickname())
                                        .build()
                        )
                        .accessToken(jwtResponseDto.getAccessToken())
                        .message("로그인 되었습니다.")
                        .build();
                return ResponseEntity.created(location).body(loginResponse);
            } else {
                throw new InvalidAuthResponseTypeException("유효하지 않은 인증반환 타입입니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("입력한 Email 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = SESSION_COOKIE_NAME, required = false) String sessionId,
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (sessionId != null) { // 세션 기반 인증의 refresh 요청인 경우 - 세션 슬라이드
            Optional<AuthResponseDto> refreshResponseDtoOpt = authService.refresh(sessionId);
            if (refreshResponseDtoOpt.isEmpty()) {
                throw new SessionNotFoundException("Refresh 요청에 대한 처리를 실패했습니다.");
            }
            SessionResponseDto sessionResponseDto = (SessionResponseDto) refreshResponseDtoOpt.get();
            // 1. 세션정보를 저장할 Cookie 구성
            ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE_NAME, sessionResponseDto.getSessionId())
                    .httpOnly(true)
                    .secure(isCookieSecure)
                    .path("/")
                    .maxAge(sessionTTlSec)
                    .sameSite("None")
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());
            URI location = URI.create("/auth/me");
            return ResponseEntity.created(location).body(null);
        } else if (refreshToken != null) { // JWT 기반 인증의 refresh 요청인 경우
            Optional<AuthResponseDto> refreshResponseDtoOpt = authService.refresh(refreshToken);
            if (refreshResponseDtoOpt.isEmpty()) {
                throw new RefreshTokenNotFoundException("Refresh 요청에 대한 처리를 실패했습니다.");
            }
            JwtResponseDto jwtResponseDto = (JwtResponseDto) refreshResponseDtoOpt.get();
            // 1. Access Token 저장을 지시하기 위한 Cookie 구성
            ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, jwtResponseDto.getAccessToken())
                    .httpOnly(true)
                    .secure(isCookieSecure)
                    .path("/")
                    .maxAge(accessTokenTtlSec)
                    .sameSite("None")
                    .build();
            response.addHeader("Set-Cookie", accessTokenCookie.toString());
            URI location = URI.create("/auth/me");
            return ResponseEntity.created(location).body(
                    JwtLoginResponseDto.builder()
                            .member(
                                    LoginMemberInfoDto.builder()
                                            .id(jwtResponseDto.getMemberId())
                                            .email(jwtResponseDto.getEmail())
                                            .nickname(jwtResponseDto.getNickname())
                                            .build()
                            )
                            .accessToken(jwtResponseDto.getAccessToken())
                            .message("새로운 Access Token이 발급되었습니다.")
                            .build()
            );
        } else {
            throw new AuthArgumentNotFoundException("인증에 필요한 인자들을 찾을 수 없습니다.");
        }
    }

    @DeleteMapping
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        // access token 관련 쿠키 무효화
        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, null)
                .httpOnly(true)
                .secure(isCookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        // refresh token 관련 쿠키 무효화
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, null)
                .httpOnly(true)
                .secure(isCookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

}
