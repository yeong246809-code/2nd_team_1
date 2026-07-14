package org.example.k_market.repository;

import org.apache.ibatis.annotations.Param;
import org.example.k_market.entity.Order;
import org.example.k_market.entity.OrderDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// OrderRepository.java
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    // OrderDetails 관련 메서드 삭제

    // FETCH JOIN을 사용하면 N+1 문제를 방지하고 한 번의 쿼리로 User 정보를 가져올 수 있습니다.
    @Query(value = "SELECT o FROM Order o LEFT JOIN FETCH o.user",
            countQuery = "SELECT count(o) FROM Order o")
    Page<Order> findAllWithJoin(Pageable pageable);

    long countByMemberNo(int memberNo);

    // [운영현황] 상태별 주문 건수 조회
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status LIKE %:status%")
    long countByStatusLike(@Param("status") String status);

    // [주요지표] 특정 날짜 구간의 주문건수 및 주문금액 합계 조회
    @Query("SELECT COUNT(o), COALESCE(SUM(o.totalPaymentPrice), 0L) " +
            "FROM Order o " +
            "WHERE o.createdAt >= :start AND o.createdAt < :end AND o.status NOT LIKE '%취소%'")
    List<Object[]> getOrderMetricsByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // [차트 통계] 최근 N일간의 주문/결제/취소 일자별 집계
    @Query("SELECT FUNCTION('DATE_FORMAT', o.createdAt, '%m-%d') AS dt, " +
            "COUNT(o) AS orderCnt, " +
            "SUM(CASE WHEN o.status LIKE '%결제%' OR o.status LIKE '%배송%' OR o.status = '완료' THEN 1 ELSE 0 END) AS payCnt, " +
            "SUM(CASE WHEN o.status LIKE '%취소%' OR o.status LIKE '%반품%' THEN 1 ELSE 0 END) AS cancelCnt " +
            "FROM Order o " +
            "WHERE o.createdAt >= :startDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', o.createdAt, '%m-%d') " +
            "ORDER BY dt ASC")
    List<Object[]> getDailySummary(@Param("startDate") LocalDateTime startDate);
}


