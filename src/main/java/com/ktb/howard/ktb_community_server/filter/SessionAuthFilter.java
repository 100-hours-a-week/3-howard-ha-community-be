package com.ktb.howard.ktb_community_server.filter;

import com.ktb.howard.ktb_community_server.auth.dto.AuthResponseDto;
import com.ktb.howard.ktb_community_server.auth.service.AuthService;
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
public class SessionAuthFilter extends BaseAuthFilter {

    private final AuthService authService;
    public static final String SESSION_COOKIE_NAME = "JSESSIONID";
    public static final String ATTRIBUTE_NAME = "AUTH_MEMBER";

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
        Optional<AuthResponseDto> sessionOpt = authService.refresh(sessionId);
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
