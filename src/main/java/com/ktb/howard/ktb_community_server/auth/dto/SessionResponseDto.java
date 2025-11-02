package com.ktb.howard.ktb_community_server.auth.dto;

import com.ktb.howard.ktb_community_server.auth.domain.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SessionResponseDto extends AuthResponseDto {

    private final String sessionId;

}
