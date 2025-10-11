package com.ktb.howard.ktb_community_server.member.service;

import com.ktb.howard.ktb_community_server.auth.dto.CustomUser;
import com.ktb.howard.ktb_community_server.image.dto.GetImageUrlResponseDto;
import com.ktb.howard.ktb_community_server.image.service.ImageService;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.dto.MemberCreateRequestDto;
import com.ktb.howard.ktb_community_server.member.dto.MemberInfoResponseDto;
import com.ktb.howard.ktb_community_server.member.exception.AlreadyUsedEmailException;
import com.ktb.howard.ktb_community_server.member.exception.AlreadyUsedNicknameException;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final ImageService imageService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member createMember(MemberCreateRequestDto request) {
        checkEmail(request.getEmail());
        checkNickname(request.getNickname());
        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();
        memberRepository.save(member);
        if (request.getProfileImageId() != null) {
            if (!imageService.isExist(request.getProfileImageId())) {
                log.error("이미지 {}가 존재하지 않습니다.", request.getProfileImageId());
                throw new IllegalStateException(String.format("이미지 %d가 존재하지 않습니다.", request.getProfileImageId()));
            }
            imageService.persistImage(request.getProfileImageId(), member, member.getId().longValue());
        }
        return member;
    }

    @Transactional(readOnly = true)
    public void checkEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new AlreadyUsedEmailException("이미 가입에 사용된 이메일 입니다.", email);
        }
    }

    @Transactional(readOnly = true)
    public void checkNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new AlreadyUsedNicknameException("이미 가입에 사용된 닉네임 입니다.", nickname);
        }
    }

    @Transactional(readOnly = true)
    public MemberInfoResponseDto getMyProfile(Integer memberId, String email, String nickname) {
        Long imageId = imageService.getMemberProfileImageId(memberId);
        String profileImageUrl = null;
        if (imageId != null) {
            GetImageUrlResponseDto imageViewUrl = imageService.getImageViewUrl(List.of(imageId));
            profileImageUrl = imageViewUrl.getImages().getFirst().url();
        }
        return new MemberInfoResponseDto(email, nickname, profileImageUrl);
    }

    @Transactional
    public void deleteMember(Integer memberId) {
        memberRepository.deleteById(memberId.longValue());
    }

}
