package com.ktb.howard.ktb_community_server.member.controller;

import com.ktb.howard.ktb_community_server.auth.dto.CustomUser;
import com.ktb.howard.ktb_community_server.member.dto.MemberCreateRequestDto;
import com.ktb.howard.ktb_community_server.member.dto.MemberInfoResponseDto;
import com.ktb.howard.ktb_community_server.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<String> createMember(@Valid @RequestBody MemberCreateRequestDto request) {
        memberService.createMember(request);
        return ResponseEntity.status(201).body("회원가입이 성공적으로 완료되었습니다");
    }

    @GetMapping("/emails/{email}")
    public ResponseEntity<String> checkEmail(
            @Email(message = "이메일 형식에 맞지 않습니다.")
            @PathVariable String email
    ) {
        memberService.checkEmail(email);
        return ResponseEntity.status(200).body("사용 가능한 이메일 입니다.");
    }

    @GetMapping("/nicknames/{nickname}")
    public ResponseEntity<String> checkNickname(
            @Pattern(
                    regexp = "^\\S{1,10}$",
                    message = "닉네임은 띄어쓰기를 포함할 수 없으며, 10글자 이내로 구성되어야 합니다."
            )
            @PathVariable String nickname
    ) {
        memberService.checkNickname(nickname);
        return ResponseEntity.status(200).body("사용 가능한 닉네임 입니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponseDto> getMyProfile(@AuthenticationPrincipal CustomUser loginMember) {
        MemberInfoResponseDto response = memberService.getMyProfile(
                loginMember.getId(),
                loginMember.getEmail(),
                loginMember.getNickname()
        );
        return ResponseEntity.status(200).body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMember(@AuthenticationPrincipal CustomUser loginMember) {
        memberService.deleteMember(loginMember.getId());
        return ResponseEntity.status(200).body("회원 탈퇴가 완료되었습니다.");
    }

}
