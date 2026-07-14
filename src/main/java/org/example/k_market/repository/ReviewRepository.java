package org.example.k_market.repository;

import org.example.k_market.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProdNoOrderByCreatedAtDesc(Long prodNo);
    Page<Review> findByMemberNoOrderByCreatedAtDesc(int memberNo, Pageable pageable);
    long countByMemberNo(int memberNo);
    // 기존에 있던 다른 메서드가 있다면 그대로 두고 위 메서드만 추가하세요.

    // 후기 많은 순
    long countByProdNo(Long prodNo);


}
