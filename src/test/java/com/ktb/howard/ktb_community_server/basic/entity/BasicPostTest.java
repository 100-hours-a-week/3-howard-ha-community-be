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
class BasicPostTest {

    @PersistenceContext
    EntityManager entityManager;

    @Test
    @DisplayName("BasicPost - 게시글을 생성한다")
    void createBasicPostTest() {
        /* given */
        BasicMember member = BasicMember.builder()
                .email("howard.ha@kakaotech.com")
                .password("12345")
                .nickname("howard.ha")
                .profileImageUrl("https://s3/profile-image/howard/howard_ha.jpg")
                .build();
        BasicPost post1 = BasicPost.builder()
                .title("테스트 게시글 제목1")
                .content("테스트 게시글 본문1")
                .writer(member)
                .build();
        BasicPost post2 = BasicPost.builder()
                .title("테스트 게시글 제목2")
                .content("테스트 게시글 본문2")
                .writer(member)
                .build();
        BasicPost post3 = BasicPost.builder()
                .title("테스트 게시글 제목3")
                .content("테스트 게시글 본문3")
                .writer(member)
                .build();

        /* when */
        entityManager.persist(member); // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행(1)
        entityManager.persist(post1);  // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행(2)
        entityManager.persist(post2);  // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행(3)
        entityManager.persist(post3);  // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행(4)
        entityManager.clear(); // SELECT Query를 의도적으로 확인하기 위한 목적으로 영속성 컨텍스트 초기화

        /* then */
        BasicPost findPost1 = entityManager.find(BasicPost.class, post1.getId());
        BasicPost findPost2 = entityManager.find(BasicPost.class, post2.getId());
        BasicPost findPost3 = entityManager.find(BasicPost.class, post3.getId());
        BasicMember findMember = entityManager.find(BasicMember.class, member.getId());
        assertThat(findPost1).isNotEqualTo(post1);
        assertThat(findPost1.getId()).isEqualTo(post1.getId());
        assertThat(findPost1.getWriter().getId()).isEqualTo(member.getId());
        assertThat(findPost2).isNotEqualTo(post1);
        assertThat(findPost2.getId()).isEqualTo(post2.getId());
        assertThat(findPost2.getWriter().getId()).isEqualTo(member.getId());
        assertThat(findPost3).isNotEqualTo(post1);
        assertThat(findPost3.getId()).isEqualTo(post3.getId());
        assertThat(findPost3.getWriter().getId()).isEqualTo(member.getId());
        /* 양방향 연관관계에 대한 업데이트 테스트 */
        assertThat(findMember.getPosts()).hasSize(3);
        assertThat(findMember.getPosts()).contains(findPost1, findPost2, findPost3);
    }

    @Test
    @DisplayName("BasicPost - 저장된 게시글 정보를 조회한다")
    void readBasicPostTest() {
        /* given */
        BasicMember member = BasicMember.builder()
                .email("howard.ha@kakaotech.com")
                .password("12345")
                .nickname("howard.ha")
                .profileImageUrl("https://s3/profile-image/howard/howard_ha.jpg")
                .build();
        BasicPost post = BasicPost.builder()
                .title("테스트 게시글 제목")
                .content("테스트 게시글 본문")
                .writer(member)
                .build();
        entityManager.persist(member); // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행(1)
        entityManager.persist(post);   // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행(2)
        entityManager.clear(); // SELECT Query를 의도적으로 확인하기 위한 목적으로 영속성 컨텍스트 초기화

        /* when */
        BasicPost findPost = entityManager.find(BasicPost.class, post.getId());
        BasicPost notExistPost = entityManager.find(BasicPost.class, post.getId() + 1); // 존재하지 않는 ID 조회

        /* then */
        assertThat(findPost.getId()).isEqualTo(post.getId());
        assertThat(findPost.getWriter().getId()).isEqualTo(member.getId());
        assertThat(notExistPost).isNull();
    }

    @Test
    @DisplayName("BasicPost - 저장된 게시글 정보를 수정한다")
    void updateBasicPostTest() {
        /* given */
        BasicMember member = BasicMember.builder()
                .email("howard.ha@kakaotech.com")
                .password("12345")
                .nickname("howard.ha")
                .profileImageUrl("https://s3/profile-image/howard/howard_ha.jpg")
                .build();
        BasicPost post = BasicPost.builder()
                .title("테스트 게시글 제목")
                .content("테스트 게시글 본문")
                .writer(member)
                .build();
        entityManager.persist(member); // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행(1)
        entityManager.persist(post);   // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행(2)

        /* when */
        BasicPost findPost = entityManager.find(BasicPost.class, post.getId());
        findPost.updateTitleAndContent("수정된 게시글 제목", "수정된 게시글 본문");
        entityManager.flush(); // Dirty Check로 업데이트 사항 DB로 동기화
        entityManager.clear(); // 업데이트 한 내용이 DB에 반영되었는 지 확인하기 위해 의도적으로 영속성 컨텍스트를 비움

        /* then */
        BasicPost updatedPost = entityManager.find(BasicPost.class, post.getId());
        assertThat(updatedPost)
                .extracting("id", "title", "content")
                .containsExactly(
                        findPost.getId(),
                        findPost.getTitle(),
                        findPost.getContent()
                );
    }

    @Test
    @DisplayName("BasicPost - 저장된 게시글 정보를 삭제한다")
    void deleteBasicPostTest() {
        /* given */
        BasicMember member = BasicMember.builder()
                .email("howard.ha@kakaotech.com")
                .password("12345")
                .nickname("howard.ha")
                .profileImageUrl("https://s3/profile-image/howard/howard_ha.jpg")
                .build();
        BasicPost post1 = BasicPost.builder()
                .title("테스트 게시글 제목1")
                .content("테스트 게시글 본문1")
                .writer(member)
                .build();
        BasicPost post2 = BasicPost.builder()
                .title("테스트 게시글 제목2")
                .content("테스트 게시글 본문2")
                .writer(member)
                .build();
        entityManager.persist(member); // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행(1)
        entityManager.persist(post1);  // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행(2)
        entityManager.persist(post2);  // 기본키 생성 전략이 AUTO_INCREMENT 이므로, 이 시점에 INSERT Query 수행(3)

        /* when */
        BasicPost findPost1 = entityManager.find(BasicPost.class, post1.getId());
        findPost1.getWriter().getPosts().remove(findPost1); // 양방향 연관관계에서 데이터의 일관성을 유지하기 위해 포함
        entityManager.remove(post1);
        entityManager.flush();
        entityManager.clear(); // 변경사항이 DB에 반영되었는 지 보기 위해서 의도적으로 영속성 컨텍스트 초기화

        /* then */
        BasicPost notExistPost = entityManager.find(BasicPost.class, post1.getId());
        BasicPost findPost = entityManager.find(BasicPost.class, post2.getId());
        assertThat(notExistPost).isNull();
        assertThat(findPost.getId()).isEqualTo(post2.getId());
        /* 양방향 연관관계에 대한 업데이트 테스트 */
        assertThat(findPost.getWriter().getPosts()).hasSize(1);
        assertThat(findPost.getWriter().getPosts()).containsExactly(findPost);
    }

}