package com.ktb.howard.ktb_community_server.member.controller;

import com.ktb.howard.ktb_community_server.auth.dto.CustomUser;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.dto.MemberCreateRequestDto;
import com.ktb.howard.ktb_community_server.member.dto.MemberInfoResponseDto;
import com.ktb.howard.ktb_community_server.member.dto.MemberUpdateRequestDto;
import com.ktb.howard.ktb_community_server.member.exception.MemberNotFoundException;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import com.ktb.howard.ktb_community_server.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
        MemberInfoResponseDto response = memberService.getProfile(loginMember.getId());
        return ResponseEntity.status(200).body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/me")
    public ResponseEntity<String> updateMember(
            @AuthenticationPrincipal CustomUser loginMember,
            @RequestBody MemberUpdateRequestDto request,
            Authentication authentication
    ) {
        memberService.updateMember(
                loginMember.getId(),
                request.getNickname(),
                request.getCurrentPassword(),
                request.getNewPassword(),
                request.getProfileImageId(),
                request.getDeleteProfileImage()
        );
        // 3. DB에서 방금 수정된 최신 사용자 정보(Member 엔티티)를 다시 조회
        Member updatedMember = memberService.findMemberById(loginMember.getId().longValue())
                .orElseThrow(() -> new MemberNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 4. 최신 DB 정보(updatedMember)로 새로운 Principal(CustomUser) 객체를 생성
        CustomUser newPrincipal = new CustomUser(
                updatedMember.getEmail(),           // username (CustomUser의 username 필드, 이메일로 가정)
                updatedMember.getPassword(),      // password (DB에 저장된 해시된 비밀번호)
                authentication.getAuthorities(),  // authorities (기존 권한)
                updatedMember.getId().intValue(), // id
                updatedMember.getEmail(),           // email
                updatedMember.getNickname()       // (닉네임 "B"가 담긴) 새 닉네임
        );

        // 5. 새 Principal로 새로운 Authentication 토큰을 생성합니다.
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                newPrincipal,                  // 새 정보가 담긴 Principal
                null,                          // Credentials(비밀번호)는 갱신 시 null로 설정
                newPrincipal.getAuthorities()  // 새 Principal의 권한
        );

        // 6. SecurityContextHolder에 새로운 인증 토큰을 설정합니다.
        //    (이 코드가 실행되면 spring-session-jdbc가 세션 DB를 갱신합니다)
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        return ResponseEntity.ok("회원 정보가 수정되었습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMember(@AuthenticationPrincipal CustomUser loginMember) {
        memberService.deleteMember(loginMember.getId());
        return ResponseEntity.status(200).body("회원 탈퇴가 완료되었습니다.");
    }

}
