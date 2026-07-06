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
public interface SalesRepository extends JpaRepository<Shop, Long> {

    /**
     * 유저님이 변경하신 DB 카멜케이스 컬럼명을 100% 유지한 통계 네이티브 쿼리
     */
    @Query(value = "SELECT s.shopNo AS shopNo, s.name AS storeName, s.bizNumber AS bizNumber, " +
            "COUNT(DISTINCT od.orderNo) AS orderCount, " +
            "COUNT(CASE WHEN od.status = '결제완료' THEN 1 END) AS paymentCompleted, " +
            "COUNT(CASE WHEN d.deliveryStatus = '배송중' THEN 1 END) AS deliveryInProgress, " +
            "COUNT(CASE WHEN d.deliveryStatus = '배송완료' THEN 1 END) AS deliveryCompleted, " +
            "COUNT(CASE WHEN od.status = '구매확정' THEN 1 END) AS purchaseConfirmed, " +
            "CAST(IFNULL(SUM(od.quantity * od.price), 0) AS SIGNED) AS totalOrderAmount, " +
            "CAST(IFNULL(SUM((od.quantity * od.price) - od.discountPrice + od.shippingFee), 0) AS SIGNED) AS totalSalesAmount " +
            "FROM shop s " +
            "LEFT JOIN order_details od ON s.shopNo = od.shopNo " +
            "LEFT JOIN `order` o ON od.orderNo = o.orderNo AND o.createdAt >= :startDate " + // orderNo, createdAt 적용
            "LEFT JOIN deliveries d ON od.orderDetailNo = d.orderDetailNo " +               // orderDetailNo 적용
            "GROUP BY s.shopNo, s.name, s.bizNumber",
            countQuery = "SELECT COUNT(*) FROM shop",
            nativeQuery = true)
    Page<Object[]> findSalesStatusSummary(@Param("startDate") LocalDateTime startDate, Pageable pageable);
}