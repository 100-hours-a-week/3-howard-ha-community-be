package com.ktb.howard.ktb_community_server.member.controller;

import com.ktb.howard.ktb_community_server.api.ApiResponse;
import com.ktb.howard.ktb_community_server.auth.annotation.AuthMember;
import com.ktb.howard.ktb_community_server.auth.dto.AuthResponseDto;
import com.ktb.howard.ktb_community_server.auth.service.AuthService;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.dto.MemberCreateRequestDto;
import com.ktb.howard.ktb_community_server.member.dto.MemberInfoResponseDto;
import com.ktb.howard.ktb_community_server.member.dto.MemberUpdateRequestDto;
import com.ktb.howard.ktb_community_server.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createMember(@Valid @RequestBody MemberCreateRequestDto request) {
        Member createdMember = memberService.createMember(request);
        ApiResponse<String> response = ApiResponse.onSuccess("회원가입이 완료되었습니다");
        URI location = URI.create("/members/" + createdMember.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/emails/{email}")
    public ResponseEntity<ApiResponse<String>> checkEmail(
            @Email(message = "이메일 형식에 맞지 않습니다.")
            @PathVariable String email
    ) {
        memberService.checkEmail(email);
        ApiResponse<String> response = ApiResponse.onSuccess("사용 가능한 이메일 입니다.");
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/nicknames/{nickname}")
    public ResponseEntity<ApiResponse<String>> checkNickname(
            @Pattern(
                    regexp = "^\\S{1,10}$",
                    message = "닉네임은 띄어쓰기를 포함할 수 없으며, 10글자 이내로 구성되어야 합니다."
            )
            @PathVariable String nickname
    ) {
        memberService.checkNickname(nickname);
        ApiResponse<String> response = ApiResponse.onSuccess("사용 가능한 닉네임 입니다.");
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponseDto>> getMyProfile(
            @AuthMember AuthResponseDto responseDto
    ) {
        MemberInfoResponseDto memberInfo = memberService.getProfile(responseDto.getMemberId());
        ApiResponse<MemberInfoResponseDto> response = ApiResponse.onSuccess(memberInfo);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<String>> updateMember(
            @AuthMember AuthResponseDto responseDto,
            @RequestBody MemberUpdateRequestDto request
    ) {
        memberService.updateMember(
                responseDto.getMemberId(),
                request.getNickname(),
                request.getCurrentPassword(),
                request.getNewPassword(),
                request.getProfileImageId(),
                request.getDeleteProfileImage()
        );
        ApiResponse<String> response = ApiResponse.onSuccess("회원 정보가 수정되었습니다.");
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteMember(
            @AuthMember AuthResponseDto responseDto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 1. 탈퇴 시 로그아웃 처리
        authService.logout(request, response);
        // 2. 저장되어 있던 회원정보 삭제
        memberService.deleteMember(responseDto.getMemberId());
        ApiResponse<String> responseBody = ApiResponse.onSuccess("회원 탈퇴가 완료되었습니다.");
        return ResponseEntity.ok().body(responseBody);
    }

}
