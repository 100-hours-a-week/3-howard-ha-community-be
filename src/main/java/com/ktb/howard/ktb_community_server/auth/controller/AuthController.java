package com.ktb.howard.ktb_community_server.auth.controller;

import com.ktb.howard.ktb_community_server.auth.dto.*;
import com.ktb.howard.ktb_community_server.auth.exception.InvalidAuthResponseTypeException;
import com.ktb.howard.ktb_community_server.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.ktb.howard.ktb_community_server.filter.SessionAuthFilter.SESSION_COOKIE_NAME;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest, HttpServletResponse response) {
        try {
            AuthResponseDto authResponseDto = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
            if (authResponseDto instanceof SessionResponseDto sessionResponseDto) {
                ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE_NAME, sessionResponseDto.getSessionId())
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(3600)
                        .sameSite("Lax")
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
            } else if (authResponseDto instanceof JwtResponseDto) {
                // TODO : 추후 JWT 기반 인증 흐름 여기에 추가하기
                return ResponseEntity.ok(null);
            } else {
                throw new InvalidAuthResponseTypeException("유효하지 않은 인증반환 타입입니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("입력한 Email 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    @DeleteMapping
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

}
