package com.ktb.howard.ktb_community_server.member.service;

import com.google.common.base.Strings;
import com.ktb.howard.ktb_community_server.image.domain.ImageType;
import com.ktb.howard.ktb_community_server.image.dto.CreateImageViewUrlRequestDto;
import com.ktb.howard.ktb_community_server.image.dto.ImageUrlResponseDto;
import com.ktb.howard.ktb_community_server.image.service.ImageService;
import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.dto.MemberCreateRequestDto;
import com.ktb.howard.ktb_community_server.member.dto.MemberInfoResponseDto;
import com.ktb.howard.ktb_community_server.member.exception.AlreadyUsedEmailException;
import com.ktb.howard.ktb_community_server.member.exception.AlreadyUsedNicknameException;
import com.ktb.howard.ktb_community_server.member.exception.MemberNotFoundException;
import com.ktb.howard.ktb_community_server.member.exception.PasswordNotMatchedException;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    public Optional<Member> findMemberById(Long memberId) {
        return memberRepository.findById(memberId);
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
    public MemberInfoResponseDto getProfile(Integer memberId) {
        CreateImageViewUrlRequestDto request = new CreateImageViewUrlRequestDto(
                ImageType.PROFILE,
                memberId.longValue()
        );
        Member member = memberRepository.findById(memberId.longValue())
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다."));
        String profileImageUrl = null;
        Long imageId = null;
        List<ImageUrlResponseDto> response = imageService.createImageViewUrl(request);
        if (!response.isEmpty()) {
            imageId = response.getFirst().imageId();
            profileImageUrl = response.getFirst().url();
        }
        return new MemberInfoResponseDto(member.getEmail(), member.getNickname(), imageId, profileImageUrl);
    }

    @Transactional
    public void updateMember(
            Integer memberId,
            String nickname,
            String currentPassword,
            String newPassword,
            Long profileImageId,
            Boolean deleteProfileImage
    ) {
        // 1. 수정할 대상인 회원정보를 불러옴
        Member member = memberRepository.findById(memberId.longValue()).orElseThrow(() -> {
            log.error("수정할 회원정보 없음 : memberId={}", memberId);
            return new MemberNotFoundException("수정할 회원정보가 없습니다.");
        });
        // 2. 닉네임에 대한 변경요청이 있는 경우 업데이트를 진행함
        if (nickname != null) {
            // 2-1. 현재 닉네임이 사용 가능한 닉네임인지 확인
            checkNickname(nickname);
            // 2-2. 사용 가능한 닉네임인 경우 닉네임 값에 대한 업데이트를 진행
            member.updateNickname(nickname);
        }
        // 3. 비밀번호에 대한 변경요청이 있는 경우 업데이트를 진행함
        if (!Strings.isNullOrEmpty(currentPassword) && !Strings.isNullOrEmpty(newPassword)) {
            // 3-1. 현재 비밀번호를 올바르게 입력했는 지 먼저 확인
            if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
                log.info("비밀번호 변경을 요청하는 기존 비밀번호 불일치 : memberId={}", memberId);
                throw new PasswordNotMatchedException("비밀번호 변경을 요청하는 기존 비밀번호 불일치");
            }
            // 3-2. 비밀번호에 대한 업데이트 진행
            member.updatePassword(passwordEncoder.encode(newPassword));
        }
        // 4. 프로필 이미지에 대한 변경요청이 있는 경우 업데이트를 진행함
        if (Boolean.TRUE.equals(deleteProfileImage)) {
            // 4-1. 프로필 이미지 삭제 요청이 있는 경우 이미지를 먼저 삭제
            List<Long> profileImage = imageService.findImageIds(ImageType.PROFILE, memberId.longValue());
            if (!profileImage.isEmpty()) {
                Long curProfileImageId = profileImage.getFirst();
                log.info("기존 프로필 이미지 삭제 영역으로 이동 : imageId={}, memberId={}", curProfileImageId, memberId);
                imageService.deleteImage(curProfileImageId);
            }
        }
        // 4-2. 새롭게 업로드한 이미지가 있는 경우 영속화 진행
        if (profileImageId != null) {
            log.info("새로운 프로필 이미지 영속화 : imageId={}, memberId={}", profileImageId, memberId);
            imageService.persistImage(profileImageId, member, member.getId().longValue());
        }
    }

    @Transactional
    public void deleteMember(Integer memberId) {
        memberRepository.deleteById(memberId.longValue());
    }

}
