package com.ktb.howard.ktb_community_server.auth.dto;

import com.ktb.howard.ktb_community_server.auth.domain.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 인증 시 사용자 정보를 저장하는 클래스
 */
@Getter
@SuperBuilder
@AllArgsConstructor
public abstract class AuthResponseDto {

    private final Integer memberId;

    private final String email;

    private final String nickname;

    private final MemberRole role;

}
