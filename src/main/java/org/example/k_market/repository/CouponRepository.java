package org.example.k_market.repository;

import org.example.k_market.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.couponNo = :couponNo")
    Optional<Coupon> findByIdForUpdate(@Param("couponNo") long couponNo);

    Optional<Coupon> findFirstByNameAndStatusOrderByCouponNoDesc(String name, String status);

    Optional<Coupon> findFirstByNameInAndStatusOrderByCouponNoDesc(List<String> names, String status);

    Optional<Coupon> findFirstByProdNoAndCouponTypeAndStatusOrderByCouponNoDesc(
            Long prodNo, String couponType, String status);

    @Query(value = """
            SELECT couponNo,
                   name,
                   benefitType,
                   benefitValue,
                   notes,
                   endDate,
                   validDays,
                   status
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

        Integer getValidDays();

        String getStatus();
    }

    Page<Coupon> findAll(Specification<Coupon> spec, Pageable pageable);
}
