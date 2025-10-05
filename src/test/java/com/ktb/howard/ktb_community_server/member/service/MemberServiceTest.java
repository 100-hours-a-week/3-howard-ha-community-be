package com.ktb.howard.ktb_community_server.member.service;

import com.ktb.howard.ktb_community_server.member.domain.Member;
import com.ktb.howard.ktb_community_server.member.dto.MemberCreateRequestDto;
import com.ktb.howard.ktb_community_server.member.exception.AlreadyUsedEmailException;
import com.ktb.howard.ktb_community_server.member.exception.AlreadyUsedNicknameException;
import com.ktb.howard.ktb_community_server.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공 - 올바른 명세를 지켜 회원가입을 요청한 경우 회원정보를 생성한다")
    void createMemberSuccessTest() {
        // given
        MemberCreateRequestDto request = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaocrop.com")
                .password("Howard12345!")
                .nickname("howard.ha")
                .build();

        // when
        Member newMember = memberService.createMember(request);

        // then
        Optional<Member> findMember = memberRepository.findById(Long.valueOf(newMember.getId()));
        assertThat(newMember.getId()).isNotNull();
        assertThat(findMember)
                .isPresent()
                .get()
                .extracting("email", "nickname")
                .containsExactly(
                        request.getEmail(),
                        request.getNickname()
                );
        assertThat(passwordEncoder.matches(request.getPassword(), findMember.get().getPassword())).isTrue();
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 가입된 이메일 주소로 가입을 시도하는 경우 409 Conflict와 함께 예외를 반환한다.")
    void createMemberFailWhenAlreadyUsedEmailTest() {
        // given
        MemberCreateRequestDto requestA = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaocrop.com")
                .password("Howard12345!")
                .nickname("howard.ha")
                .build();
        MemberCreateRequestDto requestB = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaocrop.com")
                .password("Howard12345!")
                .nickname("ryan.ha")
                .build();
        memberService.createMember(requestA);

        // when // then
        assertThatThrownBy(() -> memberService.createMember(requestB))
                .isInstanceOf(AlreadyUsedEmailException.class)
                .hasMessage("이미 가입에 사용된 이메일 입니다.");
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 가입된 닉네임으로 가입을 시도하는 경우 409 Conflict와 함께 예외를 반환한다.")
    void createMemberFailWhenAlreadyUsedNicknameTest() {
        // given
        MemberCreateRequestDto requestA = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaocrop.com")
                .password("Howard12345!")
                .nickname("howard.ha")
                .build();
        MemberCreateRequestDto requestB = MemberCreateRequestDto.builder()
                .email("ryan.ha@kakaocrop.com")
                .password("Howard12345!")
                .nickname("howard.ha")
                .build();
        memberService.createMember(requestA);

        // when // then
        assertThatThrownBy(() -> memberService.createMember(requestB))
                .isInstanceOf(AlreadyUsedNicknameException.class)
                .hasMessage("이미 가입에 사용된 닉네임 입니다.");
    }

    @Test
    @DisplayName("이메일 체크 - 회원가입에 사용된 적이 없어 사용 가능한 이메일인 경우, 어떠한 예외도 반환하지 않는다.")
    void checkEmailWhenIsUsableEmailTest() {
        // given
        String email = "ryan.ha@kakaocrop.com";

        // when // then
        assertThatCode(() -> memberService.checkEmail(email)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이메일 체크 - 이미 회원가입에 사용되어 사용할 수 없는 이메일인 경우, 409 Conflict와 함께 예외를 반환한다.")
    void checkEmailWhenIsNotUsableEmailTest() {
        // given
        Member member = Member.builder()
                .email("howard.ha@kakaocrop.com")
                .password("Howard12345!")
                .nickname("howard.ha")
                .build();
        memberRepository.save(member);

        // when // then
        assertThatThrownBy(() -> memberService.checkEmail(member.getEmail()))
                .isInstanceOf(AlreadyUsedEmailException.class)
                .hasMessage("이미 가입에 사용된 이메일 입니다.");
    }

    @Test
    @DisplayName("닉네임 체크 - 회원가입에 사용된 적이 없어 사용 가능한 닉네임의 경우, 어떠한 예외도 반환하지 않는다.")
    void checkNicknameWhenIsUsableEmailTest() {
        // given
        String nickname = "howard.ha";

        // when // then
        assertThatCode(() -> memberService.checkNickname(nickname)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("닉네임 체크 - 이미 회원가입에 사용되어 사용할 수 없는 닉네임인 경우, 409 Conflict와 함께 예외를 반환한다.")
    void checkNicknameWhenIsNotUsableEmailTest() {
        // given
        Member member = Member.builder()
                .email("howard.ha@kakaocrop.com")
                .password("Howard12345!")
                .nickname("howard.ha")
                .build();
        memberRepository.save(member);

        // when // then
        assertThatThrownBy(() -> memberService.checkNickname(member.getNickname()))
                .isInstanceOf(AlreadyUsedNicknameException.class)
                .hasMessage("이미 가입에 사용된 닉네임 입니다.");
    }

}