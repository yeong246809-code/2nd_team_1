package org.example.k_market.dao;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Users;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsersDAO {

    // JPA의 핵심인 EntityManager를 스프링이 자동으로 주입해 줍니다.
    private final EntityManager em;

    /**
     * 1. 회원 저장 (회원가입)
     */
    public void save(Users users) {
        em.persist(users);
    }

    /**
     * 2. 통합회원번호(PK)로 단건 조회
     */
    public Users findByMemberNo(Integer memberNo) {
        return em.find(Users.class, memberNo);
    }

    /**
     * 3. 로그인 아이디로 회원 조회 (JPQL 사용)
     * 로그인 기능을 구현할 때 주로 사용됩니다.
     */
    public Optional<Users> findById(String id) {
        List<Users> result = em.createQuery("select u from Users u where u.id = :id", Users.class)
                .setParameter("id", id)
                .getResultList();

        // 결과가 없을 수도 있으므로 Optional로 감싸서 반환하는 것이 안전합니다.
        return result.stream().findAny();
    }

    /**
     * 4. 전체 회원 목록 조회
     */
    public List<Users> findAll() {
        return em.createQuery("select u from Users u", Users.class)
                .getResultList();
    }

    /**
     * 5. 회원 삭제 (탈퇴 처리 등)
     */
    public void delete(Users users) {
        // 영속성 컨텍스트에 관리되는 상태(Managed)인지 확인 후 삭제
        if (em.contains(users)) {
            em.remove(users);
        } else {
            em.remove(em.merge(users));
        }
    }
}