package com.ktb.howard.ktb_community_server.member.controller;

import com.ktb.howard.ktb_community_server.auth.annotation.AuthMember;
import com.ktb.howard.ktb_community_server.auth.domain.Session;
import com.ktb.howard.ktb_community_server.auth.service.SessionService;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.dto.MemberCreateRequestDto;
import com.ktb.howard.ktb_community_server.member.dto.MemberInfoResponseDto;
import com.ktb.howard.ktb_community_server.member.dto.MemberUpdateRequestDto;
import com.ktb.howard.ktb_community_server.member.exception.MemberNotFoundException;
import com.ktb.howard.ktb_community_server.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
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
    private final SessionService sessionService;

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

    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponseDto> getMyProfile(@AuthMember Session session) {
        MemberInfoResponseDto response = memberService.getProfile(session.getMemberId());
        return ResponseEntity.status(200).body(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<String> updateMember(
            @AuthMember Session session,
            @RequestBody MemberUpdateRequestDto request
    ) {
        memberService.updateMember(
                session.getMemberId(),
                request.getNickname(),
                request.getCurrentPassword(),
                request.getNewPassword(),
                request.getProfileImageId(),
                request.getDeleteProfileImage()
        );
        // 3. DB에서 방금 수정된 최신 사용자 정보(Member 엔티티)를 다시 조회
        Member updatedMember = memberService.findMemberById(session.getMemberId().longValue())
                .orElseThrow(() -> new MemberNotFoundException("회원 정보를 찾을 수 없습니다."));
        // 4. 세션정보를 함께 업데이트
        sessionService.updateSession(session, updatedMember);
        return ResponseEntity.ok("회원 정보가 수정되었습니다.");
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMember(@AuthMember Session session) {
        sessionService.logout(session.getSessionId());
        memberService.deleteMember(session.getMemberId());
        return ResponseEntity.status(200).body("회원 탈퇴가 완료되었습니다.");
    }

}
