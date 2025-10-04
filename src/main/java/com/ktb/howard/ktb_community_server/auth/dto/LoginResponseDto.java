package com.ktb.howard.ktb_community_server.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class LoginResponseDto {

    private LoginMemberInfoDto member;

    private String message;

}
