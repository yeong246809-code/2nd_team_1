package org.example.k_market.repository;

import org.example.k_market.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Member m SET m.status = :status, m.memo = :memo WHERE m.memberNo = :memberNo")
    void updateWithdrawalStatus(
            @Param("memberNo") int memberNo,
            @Param("status") String status,
            @Param("memo") String memo);

    Page<Member> findByNameContaining(String name, Pageable pageable);
    Page<Member> findByEmailContaining(String email, Pageable pageable);
    Page<Member> findByPhoneContaining(String phone, Pageable pageable);
    List<Member> findByLastLoginAtBeforeAndStatus(LocalDateTime date, String status);
}
