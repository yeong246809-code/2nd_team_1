package org.example.k_market.repository;

import org.example.k_market.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SalesRepository extends JpaRepository<Shop, Integer> {

    @Query(value = "SELECT " +
            "    s.shopNo AS shopNo, " +
            "    s.name AS storeName, " +
            "    s.bizNumber AS bizNumber, " +
            "    COUNT(DISTINCT od.orderNo) AS orderCount, " +
            "    COUNT(CASE WHEN od.status = '결제완료' THEN 1 END) AS paymentCompleted, " +
            "    COUNT(CASE WHEN d.deliveryStatus = '배송중' THEN 1 END) AS deliveryInProgress, " +
            "    COUNT(CASE WHEN d.deliveryStatus = '배송완료' THEN 1 END) AS deliveryCompleted, " +
            "    COUNT(CASE WHEN od.status = '구매확정' THEN 1 END) AS purchaseConfirmed, " +
            "    IFNULL(SUM(od.price * od.quantity), 0) AS totalOrderAmount, " +
            "    IFNULL(SUM(CASE WHEN od.status != '주문취소' THEN (od.price * od.quantity) - od.discountPrice ELSE 0 END), 0) AS totalSalesAmount " +
            "FROM shop s " +
            "JOIN order_details od ON s.shopNo = od.shopNo " +
            "JOIN `order` o ON od.orderNo = o.orderNo " +
            "LEFT JOIN deliveries d ON od.orderDetailNo = d.orderDetailNo " +
            "WHERE o.createdAt >= :startDate " +
            "GROUP BY s.shopNo, s.name, s.bizNumber",
            countQuery = "SELECT COUNT(DISTINCT s.shopNo) FROM shop s " +
                    "JOIN order_details od ON s.shopNo = od.shopNo " +
                    "JOIN `order` o ON od.orderNo = o.orderNo " +
                    "WHERE o.createdAt >= :startDate",
            nativeQuery = true)
    Page<Object[]> findSalesStatusSummary(@Param("startDate") LocalDateTime startDate, Pageable pageable);
}