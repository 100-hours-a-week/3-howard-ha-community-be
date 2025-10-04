package com.ktb.howard.ktb_community_server.auth.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUser extends User {

    private final Integer id;

    private final String email;

    private final String nickname;

    public CustomUser(String username,
                      String password,
                      Collection<? extends GrantedAuthority> authorities,
                      Integer id,
                      String email,
                      String nickname) {
        super(username, password, authorities);
        this.id = id;
        this.email = email;
        this.nickname = nickname;
    }

}
