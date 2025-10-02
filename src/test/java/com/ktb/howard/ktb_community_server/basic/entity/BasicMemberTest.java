package com.ktb.howard.ktb_community_server.basic.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class BasicMemberTest {

    @PersistenceContext
    EntityManager entityManager;

    @Test
    @DisplayName("BasicMember - 회원정보를 생성한다")
    void createBasicMemberTest() {
        /* given */
        BasicMember member = BasicMember.builder()
                .email("howard.ha@kakaotech.com")
                .password("12345")
                .nickname("howard.ha")
                .profileImageUrl("https://s3/profile-image/howard/howard_ha.jpg")
                .build();

        /* when */
        entityManager.persist(member); // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행
        entityManager.clear(); // SELECT Query를 의도적으로 확인하기 위한 목적으로 영속성 컨텍스트 초기화

        /* then */
        BasicMember findMember = entityManager.find(BasicMember.class, member.getId());
        assertThat(findMember.getId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("BasicMember - 저장된 회원 정보를 조회한다")
    void readBasicMemberTest() {
        /* given */
        BasicMember member = BasicMember.builder()
                .email("howard.ha@kakaotech.com")
                .password("12345")
                .nickname("howard.ha")
                .profileImageUrl("https://s3/profile-image/howard/howard_ha.jpg")
                .build();
        entityManager.persist(member); // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행
        entityManager.flush();
        entityManager.clear(); // select query의 확인을 위해 의도적으로 영속성 컨텍스트를 비움

        /* when */
        BasicMember findMember = entityManager.find(BasicMember.class, member.getId());
        BasicMember notExistMember = entityManager.find(BasicMember.class, member.getId() + 1); // 존재하지 않는 ID 조회

        /* then */
        assertThat(findMember).isNotEqualTo(member);
        assertThat(findMember)
                .extracting("id", "email", "password", "nickname", "profileImageUrl")
                .containsExactly(
                        member.getId(),
                        member.getEmail(),
                        member.getPassword(),
                        member.getNickname(),
                        member.getProfileImageUrl()
                );
        assertThat(notExistMember).isNull();
    }

    @Test
    @DisplayName("BasicMember - 저장된 회원정보를 수정한다")
    void updateBasicMemberTest() {
        /* given */
        BasicMember member = BasicMember.builder()
                .email("howard.ha@kakaotech.com")
                .password("12345")
                .nickname("howard.ha")
                .profileImageUrl("https://s3/profile-image/howard/howard_ha.jpg")
                .build();
        entityManager.persist(member); // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행

        /* when */
        BasicMember findMember = entityManager.find(BasicMember.class, member.getId());
        findMember.changePassword("54321");
        entityManager.flush(); // Dirty Check로 업데이트 사항 DB로 동기화
        entityManager.clear(); // 업데이트 한 내용이 DB에 반영되었는 지 확인하기 위해 의도적으로 영속성 컨텍스트를 비움

        /* then */
        BasicMember updatedMember = entityManager.find(BasicMember.class, member.getId());
        assertThat(updatedMember)
                .extracting("id", "email", "password", "nickname", "profileImageUrl")
                .containsExactly(
                        findMember.getId(),
                        findMember.getEmail(),
                        findMember.getPassword(),
                        findMember.getNickname(),
                        findMember.getProfileImageUrl()
                );
    }

    @Test
    @DisplayName("BasicMember - 저장된 회원정보를 삭제한다")
    void deleteBasicMemberTest() {
        /* given */
        BasicMember member = BasicMember.builder()
                .email("howard.ha@kakaotech.com")
                .password("12345")
                .nickname("howard.ha")
                .profileImageUrl("https://s3/profile-image/howard/howard_ha.jpg")
                .build();
        entityManager.persist(member); // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행

        /* when */
        entityManager.remove(member);
        entityManager.flush(); // DELETE Query를 확인하기 위한 목적으로 flush 수행

        /* then */
        BasicMember findMember = entityManager.find(BasicMember.class, member.getId());
        assertThat(findMember).isNull();
    }

}