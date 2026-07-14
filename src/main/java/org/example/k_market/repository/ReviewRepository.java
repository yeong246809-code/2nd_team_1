package org.example.k_market.repository;

import org.example.k_market.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProdNoOrderByCreatedAtDesc(Long prodNo);
    Page<Review> findByMemberNoOrderByCreatedAtDesc(int memberNo, Pageable pageable);

    @Query("""
            SELECT r
            FROM Review r
            JOIN Product p ON r.prodNo = p.prodNo
            WHERE p.shopNo = :shopNo
            ORDER BY r.createdAt DESC, r.reviewNO DESC
            """)
    Page<Review> findSellerProductReviews(@Param("shopNo") int shopNo, Pageable pageable);
    long countByMemberNo(int memberNo);
    // 기존에 있던 다른 메서드가 있다면 그대로 두고 위 메서드만 추가하세요.


}
