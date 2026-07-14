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

    List<OrderDetails> findByOrderNoAndShopNo(long orderNo, long shopNo);

    List<OrderDetails> findByShopNo(long shopNo);

    boolean existsByOrderNoAndShopNo(long orderNo, long shopNo);

    boolean existsByOrderDetailNoAndShopNo(long orderDetailNo, long shopNo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OrderDetails od SET od.status = :status WHERE od.orderNo = :orderNo AND od.shopNo = :shopNo")
    int updateStatusByOrderNoAndShopNo(@Param("orderNo") long orderNo,
                                       @Param("shopNo") long shopNo,
                                       @Param("status") String status);

    @Query("""
            SELECT od
            FROM OrderDetails od
            JOIN Order o ON od.orderNo = o.orderNo
            WHERE od.shopNo = :shopNo
              AND (:searchType IS NULL OR :searchType = '' OR :keyword IS NULL OR :keyword = ''
                   OR (:searchType = 'orderNo' AND str(o.orderNo) LIKE concat('%', :keyword, '%'))
                   OR (:searchType = 'orderName' AND lower(o.orderName) LIKE lower(concat('%', :keyword, '%'))))
            ORDER BY o.createdAt DESC, od.orderDetailNo DESC
            """)
    Page<OrderDetails> findSellerOrderDetails(@Param("shopNo") long shopNo,
                                               @Param("searchType") String searchType,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);

    @Query("""
            SELECT od
            FROM OrderDetails od
            JOIN Order o ON od.orderNo = o.orderNo
            WHERE od.shopNo = :shopNo
              AND o.createdAt >= :startDateTime
              AND o.createdAt < :endDateTime
            """)
    List<OrderDetails> findSellerDetailsBetween(@Param("shopNo") long shopNo,
                                                 @Param("startDateTime") LocalDateTime startDateTime,
                                                 @Param("endDateTime") LocalDateTime endDateTime);

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

    @Query("SELECT c.name, SUM((od.price * od.quantity) - od.discountPrice) " +
            "FROM OrderDetails od " +
            "JOIN Order o ON od.orderNo = o.orderNo " +
            "JOIN Product p ON od.productNo = p.prodNo " +
            "JOIN Category c ON p.cateNo = c.cateNo " +
            "WHERE o.createdAt >= :startDate AND o.status NOT LIKE '%취소%' " +
            "GROUP BY c.name " +
            "ORDER BY SUM((od.price * od.quantity) - od.discountPrice) DESC")
    List<Object[]> getTopSalesByCategory(@Param("startDate") LocalDateTime startDate);

}
