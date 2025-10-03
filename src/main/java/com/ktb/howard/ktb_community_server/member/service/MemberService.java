package com.ktb.howard.ktb_community_server.member.service;

import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.dto.MemberCreateRequestDto;
import com.ktb.howard.ktb_community_server.member.exception.AlreadyUsedEmailException;
import com.ktb.howard.ktb_community_server.member.exception.AlreadyUsedNicknameException;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member createMember(MemberCreateRequestDto request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyUsedEmailException("이미 가입에 사용된 이메일 입니다.", request.getEmail());
        }
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new AlreadyUsedNicknameException("이미 가입에 사용된 닉네임 입니다.", request.getNickname());
        }
        Member member = Member.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .profileImageUrl(request.getProfileImageUrl())
                .build();
        memberRepository.save(member);
        return member;
    }

}
