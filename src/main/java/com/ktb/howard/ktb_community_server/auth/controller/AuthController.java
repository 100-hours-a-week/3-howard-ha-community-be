package com.ktb.howard.ktb_community_server.auth.controller;

import com.ktb.howard.ktb_community_server.api.ApiResponse;
import com.ktb.howard.ktb_community_server.api.status.ErrorStatus;
import com.ktb.howard.ktb_community_server.auth.dto.*;
import com.ktb.howard.ktb_community_server.auth.exception.AuthArgumentNotFoundException;
import com.ktb.howard.ktb_community_server.auth.exception.InvalidAuthResponseTypeException;
import com.ktb.howard.ktb_community_server.auth.exception.RefreshTokenNotFoundException;
import com.ktb.howard.ktb_community_server.auth.exception.SessionNotFoundException;
import com.ktb.howard.ktb_community_server.auth.service.AuthService;
import com.ktb.howard.ktb_community_server.auth.service.CookieProvider;
import com.ktb.howard.ktb_community_server.auth.service.JwtAuthService;
import com.ktb.howard.ktb_community_server.auth.service.SessionAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

import static com.ktb.howard.ktb_community_server.auth.service.CookieProvider.REFRESH_TOKEN_COOKIE_NAME;
import static com.ktb.howard.ktb_community_server.auth.service.CookieProvider.SESSION_ID_COOKIE_NAME;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieProvider cookieProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto loginRequest, HttpServletResponse response) {
        AuthResponseDto authResponseDto;
        try {
            authResponseDto = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
            if (authResponseDto instanceof SessionResponseDto sessionResponseDto) {
                ResponseCookie cookie = cookieProvider.createSessionCookie(sessionResponseDto.getSessionId());
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
                return ResponseEntity
                        .created(location)
                        .body(ApiResponse.onSuccess(loginResponse));
            } else if (authResponseDto instanceof JwtResponseDto jwtResponseDto) {
                ResponseCookie accessTokenCookie = cookieProvider.createAccessTokenCookie(jwtResponseDto.getAccessToken());
                response.addHeader("Set-Cookie", accessTokenCookie.toString());
                ResponseCookie refreshTokenCookie = cookieProvider.createRefreshTokenCookie(jwtResponseDto.getRefreshToken());
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
                return ResponseEntity
                        .created(location)
                        .body(ApiResponse.onSuccess(loginResponse));
            } else {
                throw new InvalidAuthResponseTypeException("유효하지 않은 인증반환 타입입니다.");
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.onFailure(ErrorStatus._BAD_REQUEST));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = SESSION_ID_COOKIE_NAME, required = false) String sessionId,
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (sessionId != null) { // 세션 기반 인증의 refresh 요청인 경우 - 세션 슬라이드
            Optional<AuthResponseDto> refreshResponseDtoOpt = authService.refresh(sessionId);
            if (refreshResponseDtoOpt.isEmpty()) {
                throw new SessionNotFoundException("Refresh 요청에 대한 처리를 실패했습니다.");
            }
            SessionResponseDto sessionResponseDto = (SessionResponseDto) refreshResponseDtoOpt.get();
            ResponseCookie cookie = cookieProvider.createSessionCookie(sessionResponseDto.getSessionId());
            response.addHeader("Set-Cookie", cookie.toString());
            URI location = URI.create("/auth/me");
            return ResponseEntity.created(location).body(null);
        } else if (refreshToken != null) { // JWT 기반 인증의 refresh 요청인 경우
            Optional<AuthResponseDto> refreshResponseDtoOpt = authService.refresh(refreshToken);
            if (refreshResponseDtoOpt.isEmpty()) {
                throw new RefreshTokenNotFoundException("Refresh 요청에 대한 처리를 실패했습니다.");
            }
            JwtResponseDto jwtResponseDto = (JwtResponseDto) refreshResponseDtoOpt.get();
            ResponseCookie cookie = cookieProvider.createAccessTokenCookie(jwtResponseDto.getAccessToken());
            response.addHeader("Set-Cookie", cookie.toString());
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
        if (authService instanceof SessionAuthService) {
            authService.logout(request, response);
            ResponseCookie sessionCookie = cookieProvider.deleteSessionCookie();
            response.addHeader("Set-Cookie", sessionCookie.toString());
            return ResponseEntity.ok("로그아웃 되었습니다.");
        } else if (authService instanceof JwtAuthService) {
            authService.logout(request, response);
            ResponseCookie accessTokenCookie = cookieProvider.deleteAccessTokenCookie();
            ResponseCookie responseCookie = cookieProvider.deleteRefreshTokenCookie();
            response.addHeader("Set-Cookie", accessTokenCookie.toString());
            response.addHeader("Set-Cookie", responseCookie.toString());
            return ResponseEntity.ok("로그아웃 되었습니다.");
        } else {
            throw new InvalidAuthResponseTypeException("유효하지 않은 인증반환 타입입니다.");
        }
    }

}
