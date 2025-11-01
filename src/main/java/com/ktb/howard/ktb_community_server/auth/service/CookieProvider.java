package com.ktb.howard.ktb_community_server.auth.service;

import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieProvider {

    @Value("${app.auth.cookie.httpOnly}")
    private boolean isHttpOnly;
    @Value("${app.auth.cookie.secure}")
    private boolean isCookieSecure;
    @Value("${app.auth.cookie.sameSite}")
    private String sameSite;

    @Value("${app.auth.session.session-ttl-sec}")
    private Long sessionTtlSec;
    @Value("${app.auth.jwt.access-token-ttl-sec}")
    private Long accessTokenTtlSec;
    @Value("${app.auth.jwt.refresh-token-ttl-sec}")
    private Long refreshTokenTtlSec;

    public static final String SESSION_ID_COOKIE_NAME = "JSESSIONID";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    public static final String BASE_COOKIE_PATH = "/";
    public static final String REFRESH_TOKEN_COOKIE_PATH = "/auth/refresh";

    /* Cookie 생성 관련 */
    public ResponseCookie createSessionCookie(String sessionId) {
        return ResponseCookie
                .from(SESSION_ID_COOKIE_NAME, sessionId)
                .httpOnly(isHttpOnly)
                .secure(isCookieSecure)
                .path(BASE_COOKIE_PATH)
                .maxAge(sessionTtlSec)
                .sameSite(sameSite)
                .build();
    }

    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return ResponseCookie
                .from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(isHttpOnly)
                .secure(isCookieSecure)
                .path(BASE_COOKIE_PATH)
                .maxAge(accessTokenTtlSec)
                .sameSite(sameSite)
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(isHttpOnly)
                .secure(isCookieSecure)
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(refreshTokenTtlSec)
                .sameSite(sameSite)
                .build();
    }

    /* Cookie 삭제 관련 */
    public ResponseCookie deleteSessionCookie() {
        return ResponseCookie
                .from(SESSION_ID_COOKIE_NAME, null)
                .httpOnly(isHttpOnly)
                .secure(isCookieSecure)
                .path(BASE_COOKIE_PATH)
                .maxAge(0)
                .sameSite(sameSite)
                .build();
    }

    public ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie
                .from(ACCESS_TOKEN_COOKIE_NAME, null)
                .httpOnly(isHttpOnly)
                .secure(isCookieSecure)
                .path(BASE_COOKIE_PATH)
                .maxAge(0)
                .sameSite(sameSite)
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, null)
                .httpOnly(isHttpOnly)
                .secure(isCookieSecure)
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(0)
                .sameSite(sameSite)
                .build();
    }

}
