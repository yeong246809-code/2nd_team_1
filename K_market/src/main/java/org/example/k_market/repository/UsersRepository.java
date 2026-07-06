package org.example.k_market.repository;

import org.example.k_market.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<엔티티타입, PK타입>
public interface UsersRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findById(String id); // 이 한 줄로 아이디 조회 기능 끝!
}