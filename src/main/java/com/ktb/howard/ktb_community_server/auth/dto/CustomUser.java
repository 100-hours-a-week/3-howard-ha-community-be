package com.ktb.howard.ktb_community_server.auth.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Objects;

@Getter
public class CustomUser extends User {

    private static final long serialVersionUID = 1L;

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

    /**
     * Spring Session이 Principal 객체의 변경을 감지할 수 있도록
     * 부모 클래스(User)의 'username' 비교를 넘어 'id'와 'nickname'까지 비교합니다.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        // 1. 부모 클래스(User)의 equals -> username 비교
        if (!super.equals(obj)) {
            return false;
        }
        // 2. 클래스 타입 확인
        if (getClass() != obj.getClass()) {
            return false;
        }
        // 3. 나머지 필드(id, email, nickname) 비교
        CustomUser other = (CustomUser) obj;
        return Objects.equals(id, other.id) &&
                Objects.equals(email, other.email) &&
                Objects.equals(nickname, other.nickname);
    }

    /**
     * equals를 오버라이드했으므로 hashCode도 반드시 오버라이드합니다.
     */
    @Override
    public int hashCode() {
        // 부모 클래스의 hashCode(username 기준)와 나머지 필드를 조합
        return Objects.hash(super.hashCode(), id, email, nickname);
    }

}
