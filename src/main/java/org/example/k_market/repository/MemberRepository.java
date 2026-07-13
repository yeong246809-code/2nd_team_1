package org.example.k_market.repository;

import org.example.k_market.entity.Member;
import org.example.k_market.service.member.MemberAccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

    // ================= [회원 관리 목록 필터 및 검색 페이징 쿼리] ================= //

    // 1. 전체 조회 + 상태 필터
    @Query("SELECT m FROM Member m WHERE (:status IS NULL OR m.status = :status)")
    Page<Member> findAllByStatus(@Param("status") MemberAccountStatus status, Pageable pageable);

    // 2. 이름 검색 + 상태 필터
    @Query("SELECT m FROM Member m WHERE (:status IS NULL OR m.status = :status) AND m.name LIKE %:keyword%")
    Page<Member> findByNameAndStatus(@Param("keyword") String keyword, @Param("status") MemberAccountStatus status, Pageable pageable);

    // 3. 이메일 검색 + 상태 필터
    @Query("SELECT m FROM Member m WHERE (:status IS NULL OR m.status = :status) AND m.email LIKE %:keyword%")
    Page<Member> findByEmailAndStatus(@Param("keyword") String keyword, @Param("status") MemberAccountStatus status, Pageable pageable);

    // 4. 휴대폰 검색 + 상태 필터
    @Query("SELECT m FROM Member m WHERE (:status IS NULL OR m.status = :status) AND m.phone LIKE %:keyword%")
    Page<Member> findByPhoneAndStatus(@Param("keyword") String keyword, @Param("status") MemberAccountStatus status, Pageable pageable);

    // 5. 아이디 검색 (Users 테이블에서 찾은 memberNo 리스트로 조회) + 상태 필터
    @Query("SELECT m FROM Member m WHERE (:status IS NULL OR m.status = :status) AND m.memberNo IN :memberNos")
    Page<Member> findByMemberNosAndStatus(@Param("memberNos") List<Integer> memberNos, @Param("status") MemberAccountStatus status, Pageable pageable);

    // ================= [기존 메소드 유지] ================= //

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Member m SET m.status = :status, m.memo = :memo WHERE m.memberNo = :memberNo")
    void updateWithdrawalStatus(
            @Param("memberNo") int memberNo,
            @Param("status") MemberAccountStatus status,
            @Param("memo") String memo);

    Page<Member> findByNameContaining(String name, Pageable pageable);
    Page<Member> findByEmailContaining(String email, Pageable pageable);
    Page<Member> findByPhoneContaining(String phone, Pageable pageable);
    Optional<Member> findFirstByNameAndEmailIgnoreCase(String name, String email);
    List<Member> findByLastLoginAtBeforeAndStatus(
            LocalDateTime date,
            MemberAccountStatus status);

    @Query("""
            SELECT m
            FROM Member m
            WHERE m.name = :name
              AND REPLACE(COALESCE(m.phone, ''), '-', '') = :phone
            """)
    Optional<Member> findFirstByNameAndNormalizedPhone(
            @Param("name") String name,
            @Param("phone") String phone);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Member m SET m.lastLoginAt = :lastLoginAt WHERE m.memberNo = :memberNo")
    void updateLastLoginAt(
            @Param("memberNo") int memberNo,
            @Param("lastLoginAt") LocalDateTime lastLoginAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Member m SET m.status = :status WHERE m.memberNo = :memberNo")
    void updateStatus(
            @Param("memberNo") int memberNo,
            @Param("status") MemberAccountStatus status);
}
