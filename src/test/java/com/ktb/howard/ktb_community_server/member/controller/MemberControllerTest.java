package com.ktb.howard.ktb_community_server.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktb.howard.ktb_community_server.config.JpaConfig;
import com.ktb.howard.ktb_community_server.config.SecurityConfig;
import com.ktb.howard.ktb_community_server.member.dto.MemberCreateRequestDto;
import com.ktb.howard.ktb_community_server.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MemberController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JpaConfig.class
        )
)
@Import(SecurityConfig.class)
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 요청 성공 - 요청 데이터가 모두 정책을 준수한 경우 201 Created를 반환한다")
    void createMemberRequestSuccessTest() throws Exception {
        // given
        MemberCreateRequestDto requestDto = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaotech.com")
                .password("Howard12345!")
                .nickname("howard.ha")
                .profileImageUrl("https://example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        );

        // then
        resultActions.andExpect(status().isCreated());
    }

    @Test
    @DisplayName("회원가입 요청 실패 - 이메일을 제공하지 않은 경우 400 Bad Request와 관련 에러 메시지를 반환한다")
    void createMemberRequestFailWhenEmailIsNullTest() throws Exception {
        // given
        MemberCreateRequestDto requestDto = MemberCreateRequestDto.builder()
                .password("Howard12345!")
                .nickname("howard.ha")
                .profileImageUrl("https://example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("이메일은 필수 입력 값입니다."));
    }

    @Test
    @DisplayName("회원가입 요청 실패 - 이메일 형식을 준수하지 않은 경우 400 Bad Request와 관련 에러 메시지를 반환한다")
    void createMemberRequestFailWhenEmailDoNotFollowPolicyTest() throws Exception {
        // given
        MemberCreateRequestDto requestDto = MemberCreateRequestDto.builder()
                .email("howard.ha|kakaotech.com")
                .password("Howard12345!")
                .nickname("howard.ha")
                .profileImageUrl("https://example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("이메일 형식에 맞지 않습니다."));
    }

    @Test
    @DisplayName("회원가입 요청 실패 - 비밀번호를 제공하지 않은 경우 400 Bad Request와 관련 에러 메시지를 반환한다")
    void createMemberRequestFailWhenPasswordIsNullTest() throws Exception {
        MemberCreateRequestDto requestDto = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaotech.com")
                .nickname("howard.ha")
                .profileImageUrl("https://example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("password"))
                .andExpect(jsonPath("$.errors[0].message").value("비밀번호는 필수 입력 값입니다."));
    }

    @Test
    @DisplayName("회원가입 요청 실패 - 비밀번호의 길이가 최소길이 8 미만인 경우")
    void createMemberRequestFailWhenPasswordIsTooShortTest() throws Exception {
        // given
        MemberCreateRequestDto requestDto = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaotech.com")
                .password("Ab1!")
                .nickname("howard.ha")
                .profileImageUrl("https://example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("password"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("비밀번호는 8~20자의 영문 대/소문자, 숫자, 특수문자(!@#$%^&*())를 사용해야 합니다."));
    }

    @Test
    @DisplayName("회원가입 요청 실패 - 비밀번호의 길이가 최대길이 20 초과인 경우")
    void createMemberRequestFailWhenPasswordIsTooLongTest() throws Exception {
        // given
        MemberCreateRequestDto requestDto = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaotech.com")
                .password("Password1234567890long!")
                .nickname("howard.ha")
                .profileImageUrl("https://example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("password"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("비밀번호는 8~20자의 영문 대/소문자, 숫자, 특수문자(!@#$%^&*())를 사용해야 합니다."));
    }

    @Test
    @DisplayName("회원가입 요청 실패 - 영어대문자, 소문자, 숫자, 특수문자로 구성되지 않은 경우 400 Bad Request와 관련 에러 메시지를 반환한다")
    void createMemberRequestFailWhenPasswordDoNotFollowPolicyTest() throws Exception {
        // given
        MemberCreateRequestDto requestDto = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaotech.com")
                .password("12345")
                .nickname("howard.ha")
                .profileImageUrl("https://example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("password"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("비밀번호는 8~20자의 영문 대/소문자, 숫자, 특수문자(!@#$%^&*())를 사용해야 합니다."));
    }

    @Test
    @DisplayName("회원가입 요청 실패 - 닉네임을 제공하지 않은 경우 400 Bad Request와 관련 에러 메시지를 반환한다")
    void createMemberRequestFailWhenNicknameIsNullTest() throws Exception {
        // given
        MemberCreateRequestDto requestDto = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaotech.com")
                .password("Howard12345!")
                .profileImageUrl("https://example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("nickname"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("닉네임은 필수 입력 값입니다."));
    }

    @Test
    @DisplayName("회원가입 요청 실패 - 닉네임에 빈칸이 포함되는 경우 400 Bad Request와 관련 에러 메시지를 반환한다")
    void createMemberRequestFailWhenNicknameContainsSpaceTest() throws Exception {
        // given
        MemberCreateRequestDto requestDto = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaotech.com")
                .password("Howard12345!")
                .nickname("howard ha")
                .profileImageUrl("https://example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("nickname"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("닉네임은 띄어쓰기를 포함할 수 없으며, 10글자 이내로 구성되어야 합니다."));
    }

    @Test
    @DisplayName("회원가입 요청 실패 - 닉네임의 길이가 10을 초과하는 경우 400 Bad Request와 관련 에러 메시지를 반환한다")
    void createMemberRequestFailWhenNicknameTooLongTest() throws Exception {
        // given
        MemberCreateRequestDto requestDto = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaotech.com")
                .password("Howard12345!")
                .nickname("howard.ha12345")
                .profileImageUrl("https://example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("nickname"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("닉네임은 띄어쓰기를 포함할 수 없으며, 10글자 이내로 구성되어야 합니다."));
    }

    @Test
    @DisplayName("회원가입 요청 실패 - 프로필 이미지 URL의 길이가 1024를 초과하는 경우 400 Bad Request와 관련 에러 메시지를 반환한다")
    void createMemberRequestFailedWhenTooLongProfileImageUrl() throws Exception {
        // given
        MemberCreateRequestDto requestDto = MemberCreateRequestDto.builder()
                .email("howard.ha@kakaotech.com")
                .password("Howard12345!")
                .nickname("howard.ha")
                .profileImageUrl("a".repeat(1025))
                .build();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("profileImageUrl"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("프로필 이미지 URL의 길이는 1024를 초과할 수 없습니다."));
    }

    @Test
    @DisplayName("이메일 체크 - 이메일 형식을 준수하지 않은 경우 400 Bad Request와 관련 에러 메시지를 반환한다")
    void checkEmailWhenEmailDoNotFollowPolicyTest() throws Exception {
        // given
        String email = "howard.ha@@kakaotech.com";

        // when
        ResultActions resultActions = mockMvc.perform(get("/members/emails/{email}", email));

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("이메일 형식에 맞지 않습니다."));
    }

    @Test
    @DisplayName("이메일 체크 - 이메일 형식을 준수하지 않은 경우 400 Bad Request와 관련 에러 메시지를 반환한다")
    void checkNicknameWhenNicknameDoNotFollowPolicyTest() throws Exception {
        // given
        String nicknameA = "howard ha";   // 닉네임에 빈칸을 포함
        String nicknameB = "howard.park"; // 닉네임 10글자를 초과함

        // when
        ResultActions resultActionsA = mockMvc.perform(get("/members/nicknames/{nickname}", nicknameA));
        ResultActions resultActionsB = mockMvc.perform(get("/members/nicknames/{nickname}", nicknameB));

        // then
        resultActionsA
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("nickname"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("닉네임은 띄어쓰기를 포함할 수 없으며, 10글자 이내로 구성되어야 합니다."));
        resultActionsB
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("nickname"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("닉네임은 띄어쓰기를 포함할 수 없으며, 10글자 이내로 구성되어야 합니다."));
    }

}