package org.example.k_market.repository;

import org.example.k_market.entity.Order;
import org.example.k_market.entity.OrderDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}


