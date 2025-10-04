package com.ktb.howard.ktb_community_server.auth.controller;

import com.ktb.howard.ktb_community_server.auth.dto.CustomUser;
import com.ktb.howard.ktb_community_server.auth.dto.LoginMemberInfoDto;
import com.ktb.howard.ktb_community_server.auth.dto.LoginRequestDto;
import com.ktb.howard.ktb_community_server.auth.dto.LoginResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionStrategy;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            sessionStrategy.onAuthentication(authentication, request, response);
            securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

            URI location = URI.create("/auth/me");
            CustomUser loginMember = (CustomUser) authentication.getPrincipal();
            LoginResponseDto loginResponse = LoginResponseDto.builder()
                    .member(
                            LoginMemberInfoDto.builder()
                                    .id(loginMember.getId())
                                    .email(loginMember.getUsername())
                                    .nickname(loginMember.getNickname())
                                    .build()
                    )
                    .message("로그인 되었습니다.")
                    .build();

            return ResponseEntity.created(location).body(loginResponse);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("입력한 Email 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    @DeleteMapping
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 현재 사용자의 인증 정보를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증 정보가 존재하면 로그아웃을 처리합니다.
        if (authentication != null) {
            // SecurityContextLogoutHandler를 사용하여 세션을 무효화하고 컨텍스트를 클리어합니다.
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

}
