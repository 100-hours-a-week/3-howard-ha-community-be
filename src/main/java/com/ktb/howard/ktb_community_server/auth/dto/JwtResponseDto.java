package com.ktb.howard.ktb_community_server.auth.dto;

import com.ktb.howard.ktb_community_server.auth.domain.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class JwtResponseDto extends AuthResponseDto {

    private final String accessToken;

    private final String refreshToken;

}
