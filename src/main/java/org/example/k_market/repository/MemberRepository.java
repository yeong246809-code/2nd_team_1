package org.example.k_market.repository;

import org.example.k_market.entity.Member;
import org.example.k_market.service.member.MemberAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Member m SET m.status = :status, m.memo = :memo WHERE m.memberNo = :memberNo")
    void updateWithdrawalStatus(
            @Param("memberNo") int memberNo,
            @Param("status") MemberAccountStatus status,
            @Param("memo") String memo);
}
