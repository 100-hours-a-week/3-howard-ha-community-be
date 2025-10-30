package com.ktb.howard.ktb_community_server.filter;

import com.ktb.howard.ktb_community_server.auth.domain.MemberRole;
import com.ktb.howard.ktb_community_server.auth.dto.JwtResponseDto;
import com.ktb.howard.ktb_community_server.auth.service.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthFilter extends BaseAuthFilter {

    private final JwtProvider jwtProvider;
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String ATTRIBUTE_NAME = "AUTH_MEMBER";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Optional<String> token = extractToken(request);
        // 인증 토큰이 없음
        if (token.isEmpty()) {
            sendUnauthorizedError(response, "올바르지 않은 요청입니다.");
            return;
        }
        // 유효하지 않은 요청
        if (!validateAndSetAttributes(token.get(), request)) {
            sendUnauthorizedError(response, "요청 내용이 유효하지 않습니다.");
            return;
        }
        filterChain.doFilter(request, response);
    }

    // 토큰 추출 (헤더 우선, 쿠키 다음)
    private Optional<String> extractToken(HttpServletRequest request) {
        return extractTokenFromHeader(request)
                .or(() -> extractTokenFromCookie(request));
    }

    // 헤더에서 토큰 추출
    private Optional<String> extractTokenFromHeader(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7));
    }

    // 쿠키에서 토큰 추출
    private Optional<String> extractTokenFromCookie(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    // 토큰 검증 및 요청 속성 설정
    private boolean validateAndSetAttributes(String token, HttpServletRequest request) {
        try {
            var jws = jwtProvider.parse(token);
            Claims body = jws.getBody();
            JwtResponseDto responseDto = JwtResponseDto.builder()
                    .memberId(Integer.parseInt(body.getSubject()))
                    .email(body.get("email").toString())
                    .role(MemberRole.USER)
                    .build();
            request.setAttribute(ATTRIBUTE_NAME, responseDto);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\": 401, \"message\": \"" + message + "\"}");
    }

}
