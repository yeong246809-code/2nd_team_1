package org.example.k_market.repository;

import org.example.k_market.entity.Order;
import org.example.k_market.entity.OrderDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = """
            SELECT o FROM Order o
            LEFT JOIN FETCH o.user
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR (:searchType = 'orderNo' AND str(o.orderNo) LIKE concat('%', :keyword, '%'))
                   OR (:searchType = 'memberId' AND lower(o.user.id) LIKE lower(concat('%', :keyword, '%'))))
            """,
            countQuery = """
            SELECT count(o) FROM Order o
            LEFT JOIN o.user u
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR (:searchType = 'orderNo' AND str(o.orderNo) LIKE concat('%', :keyword, '%'))
                   OR (:searchType = 'memberId' AND lower(u.id) LIKE lower(concat('%', :keyword, '%'))))
            """)
    Page<Order> findAdminOrders(@Param("searchType") String searchType,
                                @Param("keyword") String keyword,
                                Pageable pageable);

    @Query(value = """
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.user
            JOIN OrderDetails od ON od.orderNo = o.orderNo
            WHERE od.shopNo = :shopNo
              AND (:keyword IS NULL OR :keyword = ''
                   OR (:searchType = 'orderNo' AND str(o.orderNo) LIKE concat('%', :keyword, '%'))
                   OR (:searchType = 'orderName' AND lower(o.orderName) LIKE lower(concat('%', :keyword, '%')))
                   OR (:searchType = 'memberId' AND lower(o.user.id) LIKE lower(concat('%', :keyword, '%'))))
            """,
            countQuery = """
            SELECT COUNT(DISTINCT o.orderNo) FROM Order o
            JOIN OrderDetails od ON od.orderNo = o.orderNo
            WHERE od.shopNo = :shopNo
              AND (:keyword IS NULL OR :keyword = ''
                   OR (:searchType = 'orderNo' AND str(o.orderNo) LIKE concat('%', :keyword, '%'))
                   OR (:searchType = 'orderName' AND lower(o.orderName) LIKE lower(concat('%', :keyword, '%')))
                   OR (:searchType = 'memberId' AND lower(o.user.id) LIKE lower(concat('%', :keyword, '%'))))
            """)
    Page<Order> findSellerOrders(@Param("shopNo") long shopNo,
                                 @Param("searchType") String searchType,
                                 @Param("keyword") String keyword,
                                 Pageable pageable);

    long countByMemberNo(int memberNo);

    Page<Order> findByMemberNoOrderByCreatedAtDesc(int memberNo, Pageable pageable);

    Page<Order> findByMemberNoAndCreatedAtBetweenOrderByCreatedAtDesc(
            int memberNo,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Pageable pageable
    );

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


