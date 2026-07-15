package org.example.k_market.repository;

import org.example.k_market.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Query(value = """
            SELECT couponNo,
                   name,
                   benefitType,
                   benefitValue,
                   notes,
                   endDate
            FROM coupon
            WHERE couponNo = :couponNo
            """, nativeQuery = true)
    Optional<CouponSummary> findSummaryByCouponNo(@Param("couponNo") long couponNo);

    interface CouponSummary {
        Long getCouponNo();

        String getName();

        String getBenefitType();

        Integer getBenefitValue();

        String getNotes();

        LocalDate getEndDate();
    }

    Page<Coupon> findAll(Specification<Coupon> spec, Pageable pageable);
}
