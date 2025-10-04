package com.ktb.howard.ktb_community_server.member.controller;

import com.ktb.howard.ktb_community_server.member.dto.MemberCreateRequestDto;
import com.ktb.howard.ktb_community_server.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

}
