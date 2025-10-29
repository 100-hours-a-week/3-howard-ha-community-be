package com.ktb.howard.ktb_community_server.auth.service;

import com.ktb.howard.ktb_community_server.auth.dto.AuthResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

/**
 * 인증 서비스를 이용하기 위한 진입점이 되는 부분
 */
public interface AuthService {

    // 로그인 기능 필요 시 호출
    AuthResponseDto login(String email, String password);

    // 세션 슬라이드 or refreshToken을 통한 accessToken 발급 시 호출
    Optional<AuthResponseDto> refresh(String authId);

    // 로그아웃 기능 필요 시 호출
    void logout(HttpServletRequest request, HttpServletResponse response);

}
