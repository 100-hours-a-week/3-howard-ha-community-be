package com.ktb.howard.ktb_community_server.auth.controller;

import com.ktb.howard.ktb_community_server.auth.domain.Session;
import com.ktb.howard.ktb_community_server.auth.dto.LoginMemberInfoDto;
import com.ktb.howard.ktb_community_server.auth.dto.LoginRequestDto;
import com.ktb.howard.ktb_community_server.auth.dto.LoginResponseDto;
import com.ktb.howard.ktb_community_server.auth.service.SessionService;
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

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDto loginRequest,
            HttpServletResponse response
    ) {
        try {
            Session newSession = sessionService.login(loginRequest.getEmail(), loginRequest.getPassword());
            ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE_NAME, newSession.getSessionId())
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
                                    .id(newSession.getMemberId())
                                    .email(newSession.getEmail())
                                    .nickname(newSession.getNickname())
                                    .build()
                    )
                    .message("로그인 되었습니다.")
                    .build();
            return ResponseEntity.created(location).body(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("입력한 Email 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    @DeleteMapping
    public ResponseEntity<String> logout(
            @CookieValue(name = SESSION_COOKIE_NAME, required = false) String sessionId,
            HttpServletResponse response
    ) {
        sessionService.logout(sessionId);
        invalidateCookie(response);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    private void invalidateCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE_NAME, null)
                .maxAge(0)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

}
