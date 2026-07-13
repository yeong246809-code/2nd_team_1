package org.example.k_market.repository;

import org.example.k_market.entity.OrderDetails;
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
public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Long> {
    List<OrderDetails> findByOrderNo(long orderNo);

    @Query("""
            SELECT od
            FROM OrderDetails od
            JOIN Order o ON od.orderNo = o.orderNo
            WHERE o.memberNo = :memberNo
            ORDER BY o.createdAt DESC, od.orderDetailNo DESC
            """)
    Page<OrderDetails> findByMemberNo(@Param("memberNo") int memberNo, Pageable pageable);

    @Query("""
            SELECT od
            FROM OrderDetails od
            JOIN Order o ON od.orderNo = o.orderNo
            WHERE o.memberNo = :memberNo
              AND (:startDateTime IS NULL OR o.createdAt >= :startDateTime)
              AND (:endDateTime IS NULL OR o.createdAt < :endDateTime)
            ORDER BY o.createdAt DESC, od.orderDetailNo DESC
            """)
    Page<OrderDetails> findByMemberNoAndCreatedAtBetween(
            @Param("memberNo") int memberNo,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable);

    @Query("""
            SELECT COUNT(od)
            FROM OrderDetails od
            JOIN Order o ON od.orderNo = o.orderNo
            WHERE o.memberNo = :memberNo
            """)
    long countByMemberNo(@Param("memberNo") int memberNo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OrderDetails od SET od.status = :status WHERE od.orderDetailNo = :orderDetailNo")
    void updateStatus(@Param("orderDetailNo") long orderDetailNo, @Param("status") String status);
}
