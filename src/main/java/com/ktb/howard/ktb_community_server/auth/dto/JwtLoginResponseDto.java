package com.ktb.howard.ktb_community_server.auth.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class JwtLoginResponseDto extends LoginResponseDto {

    private String accessToken;

}
