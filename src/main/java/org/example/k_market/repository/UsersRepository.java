package org.example.k_market.repository;

import org.example.k_market.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// JpaRepository<엔티티타입, PK타입>
public interface UsersRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findById(String id); // 이 한 줄로 아이디 조회 기능 끝!

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Users u SET u.id = :id WHERE u.memberNo = :memberNo")
    void updateLoginId(@Param("memberNo") int memberNo, @Param("id") String id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Users u SET u.status = :status WHERE u.memberNo = :memberNo")
    void updateStatus(@Param("memberNo") int memberNo, @Param("status") String status);

    Page<Users> findByIdContaining(String id, Pageable pageable);
    List<Users> findAllByMemberNoIn(List<Integer> memberNos);
}
