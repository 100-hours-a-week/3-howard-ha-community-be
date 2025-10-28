package com.ktb.howard.ktb_community_server.filter;

import com.ktb.howard.ktb_community_server.auth.domain.Session;
import com.ktb.howard.ktb_community_server.auth.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    private final SessionService sessionService;
    public static final String SESSION_COOKIE_NAME = "JSESSIONID";
    public static final String ATTRIBUTE_NAME = "AUTH_MEMBER";

    // 1. filter 제외 경로 설정
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // OPTIONS 메서드는 모든 경로에 대해 항상 허용
        if ("OPTIONS".equals(method)) {
            return true; // true = 필터링 안 함 (허용)
        }

        // Swagger, error와 관련된 경로에 대해 항상 허용
        if (path.equals("/") ||
                path.equals("/auth") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/error")) {
            return true;
        }

        // 회원가입 API 경로에 대한 허용
        if ("POST".equals(method) && path.equals("/members")) {
            return true;
        }

        // 이메일 확인 API 경로에 대한 허용
        if ("GET".equals(method) && path.startsWith("/members/emails")) {
            return true;
        }

        // 닉네임 확인 API 경로에 대한 허용
        if ("GET".equals(method) && path.startsWith("/members/nicknames")) {
            return true;
        }

        // 이미지 업로드 URL 발급 API 경로에 대한 허용
        if ("POST".equals(method) && path.startsWith("/images/upload-urls")) {
            return true;
        }

        // 정책 페이지 경로에 대한 허용
        if ("GET".equals(method) && path.startsWith("/policy")) {
            return true;
        }

        return false;
    }

    // 2. 실제 필터링 로직
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Optional<String> sessionIdOpt = getSessionId(request);
        if (sessionIdOpt.isEmpty()) {
            sendUnauthorizedError(response, "세션 정보가 없습니다. 로그인해주세요.");
            return;
        }
        String sessionId = sessionIdOpt.get(); // session id를 추출
        Optional<Session> sessionOpt = sessionService.findSessionAndSlide(sessionId);
        if (sessionOpt.isEmpty()) {
            sendUnauthorizedError(response, "유효하지 않은 세션입니다. 다시 로그인해주세요.");
            return;
        }
        request.setAttribute(ATTRIBUTE_NAME, sessionOpt.get());
        filterChain.doFilter(request, response);
    }

    private Optional<String> getSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(SESSION_COOKIE_NAME))
                .map(Cookie::getValue) // 쿠키의 값(세션 ID)을 추출
                .findFirst();
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\": 401, \"message\": \"" + message + "\"}");
    }

}
