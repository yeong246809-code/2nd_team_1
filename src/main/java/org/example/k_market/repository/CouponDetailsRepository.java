package org.example.k_market.repository;

import org.example.k_market.entity.CouponDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponDetailsRepository extends JpaRepository<CouponDetails, Long> {
    Page<CouponDetails> findByMemberNoOrderByIssuedAtDesc(int memberNo, Pageable pageable);

    long countByMemberNoAndIsUsedIgnoreCase(int memberNo, String isUsed);

    long countByCouponNo(long couponNo);

    long countByCouponNoAndIsUsed(long couponNo, String isUsed);
}
