package com.ktb.howard.ktb_community_server.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.filter.OncePerRequestFilter;

public abstract class BaseAuthFilter extends OncePerRequestFilter {

    @Override // 필터링 대상 제외경로 명세
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
        if ("POST".equals(method) && path.equals("/api/members")) {
            return true;
        }

        // 이메일 확인 API 경로에 대한 허용
        if ("GET".equals(method) && path.startsWith("/api/members/emails")) {
            return true;
        }

        // 닉네임 확인 API 경로에 대한 허용
        if ("GET".equals(method) && path.startsWith("/api/members/nicknames")) {
            return true;
        }

        // 이미지 업로드 URL 발급 API 경로에 대한 허용
        if ("POST".equals(method) && path.startsWith("/api/images/upload-urls")) {
            return true;
        }

        // 정책 페이지 경로에 대한 허용
        if ("GET".equals(method) && path.startsWith("/policy")) {
            return true;
        }

        // 로그인 대한 요청
        if ("POST".equals(method) && path.startsWith("/api/auth")) {
            return true;
        }


        // refresh에 대한 요청
        if ("POST".equals(method) && path.startsWith("/api/auth/refresh")) {
            return true;
        }

        // health-check에 대한 요청
        if ("GET".equals(method) && path.startsWith("/actuator/health")) {
            return true;
        }

        return false;
    }

}
