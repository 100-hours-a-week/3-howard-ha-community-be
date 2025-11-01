package com.ktb.howard.ktb_community_server.auth.controller;

import com.ktb.howard.ktb_community_server.api.ApiResponse;
import com.ktb.howard.ktb_community_server.auth.dto.*;
import com.ktb.howard.ktb_community_server.auth.exception.InvalidAuthResponseTypeException;
import com.ktb.howard.ktb_community_server.auth.exception.RefreshTokenNotFoundException;
import com.ktb.howard.ktb_community_server.auth.exception.SessionNotFoundException;
import com.ktb.howard.ktb_community_server.auth.service.AuthService;
import com.ktb.howard.ktb_community_server.auth.provider.CookieProvider;
import com.ktb.howard.ktb_community_server.auth.service.JwtAuthService;
import com.ktb.howard.ktb_community_server.auth.service.SessionAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

import static com.ktb.howard.ktb_community_server.api.AuthErrorCode.*;
import static com.ktb.howard.ktb_community_server.auth.provider.CookieProvider.REFRESH_TOKEN_COOKIE_NAME;
import static com.ktb.howard.ktb_community_server.auth.provider.CookieProvider.SESSION_ID_COOKIE_NAME;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieProvider cookieProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @RequestBody LoginRequestDto loginRequest,
            HttpServletResponse response
    ) {
        AuthResponseDto authResponseDto = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
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
            throw new InvalidAuthResponseTypeException(INVALID_AUTH_RESPONSE_TYPE);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(
            @CookieValue(name = SESSION_ID_COOKIE_NAME, required = false) String sessionId,
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (authService instanceof SessionAuthService) {
            Optional<AuthResponseDto> refreshResponseDtoOpt = authService.refresh(sessionId);
            if (refreshResponseDtoOpt.isEmpty()) {
                throw new SessionNotFoundException(SESSION_NOT_FOUND);
            }
            SessionResponseDto sessionResponseDto = (SessionResponseDto) refreshResponseDtoOpt.get();
            ResponseCookie cookie = cookieProvider.createSessionCookie(sessionResponseDto.getSessionId());
            response.addHeader("Set-Cookie", cookie.toString());
            URI location = URI.create("/auth/me");
            ApiResponse<JwtLoginResponseDto> responseBody = ApiResponse.onSuccess();
            return ResponseEntity.created(location).body(responseBody);
        } else if (authService instanceof JwtAuthService) {
            Optional<AuthResponseDto> refreshResponseDtoOpt = authService.refresh(refreshToken);
            if (refreshResponseDtoOpt.isEmpty()) {
                throw new RefreshTokenNotFoundException(REFRESH_TOKEN_NOT_FOUND);
            }
            JwtResponseDto jwtResponseDto = (JwtResponseDto) refreshResponseDtoOpt.get();
            ResponseCookie cookie = cookieProvider.createAccessTokenCookie(jwtResponseDto.getAccessToken());
            response.addHeader("Set-Cookie", cookie.toString());
            URI location = URI.create("/auth/me");
            JwtLoginResponseDto jwtLoginResponseDto = JwtLoginResponseDto.builder()
                    .member(
                            LoginMemberInfoDto.builder()
                                    .id(jwtResponseDto.getMemberId())
                                    .email(jwtResponseDto.getEmail())
                                    .nickname(jwtResponseDto.getNickname())
                                    .build()
                    )
                    .accessToken(jwtResponseDto.getAccessToken())
                    .message("새로운 Access Token이 발급되었습니다.")
                    .build();
            ApiResponse<JwtLoginResponseDto> responseBody = ApiResponse.onSuccess(jwtLoginResponseDto);
            return ResponseEntity.created(location).body(responseBody);
        } else {
            throw new InvalidAuthResponseTypeException(INVALID_AUTH_RESPONSE_TYPE);
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
        if (authService instanceof SessionAuthService) {
            authService.logout(request, response);
            ResponseCookie sessionCookie = cookieProvider.deleteSessionCookie();
            response.addHeader("Set-Cookie", sessionCookie.toString());
            return ResponseEntity.ok(ApiResponse.onSuccess("로그아웃 되었습니다."));
        } else if (authService instanceof JwtAuthService) {
            authService.logout(request, response);
            ResponseCookie accessTokenCookie = cookieProvider.deleteAccessTokenCookie();
            ResponseCookie responseCookie = cookieProvider.deleteRefreshTokenCookie();
            response.addHeader("Set-Cookie", accessTokenCookie.toString());
            response.addHeader("Set-Cookie", responseCookie.toString());
            return ResponseEntity.ok(ApiResponse.onSuccess("로그아웃 되었습니다."));
        } else {
            throw new InvalidAuthResponseTypeException(INVALID_AUTH_RESPONSE_TYPE);
        }
    }

}
