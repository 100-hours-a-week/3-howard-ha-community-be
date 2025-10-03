package com.ktb.howard.ktb_community_server.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginRequestDto {

    private String email;

    private String password;

}
