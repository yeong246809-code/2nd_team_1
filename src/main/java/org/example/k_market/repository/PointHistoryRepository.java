package org.example.k_market.repository;

import org.example.k_market.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    // memberNo 리스트에 해당하는 포인트 내역 조회 (검색용)
    Page<PointHistory> findByMemberNoIn(List<Integer> memberNos, Pageable pageable);

    Page<PointHistory> findByMemberNoOrderByCreatedAtDesc(int memberNo, Pageable pageable);

    @Query("""
            SELECT p
            FROM PointHistory p
            WHERE p.memberNo = :memberNo
              AND (:startDateTime IS NULL OR p.createdAt >= :startDateTime)
              AND (:endDateTime IS NULL OR p.createdAt < :endDateTime)
            ORDER BY p.createdAt DESC, p.pointNo DESC
            """)
    Page<PointHistory> findByMemberNoAndCreatedAtBetween(
            @Param("memberNo") int memberNo,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable);
}