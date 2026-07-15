package org.example.k_market.repository;

import org.example.k_market.entity.CouponDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface CouponDetailsRepository extends JpaRepository<CouponDetails, Long>, JpaSpecificationExecutor<CouponDetails> {
    Page<CouponDetails> findByMemberNoOrderByIssuedAtDesc(int memberNo, Pageable pageable);

    long countByMemberNoAndIsUsedIgnoreCase(int memberNo, String isUsed);

    long countByCouponNo(long couponNo);

    long countByCouponNoAndIsUsed(long couponNo, String isUsed);

    boolean existsByCouponNoAndMemberNo(long couponNo, int memberNo);

    boolean existsByCouponNoAndMemberNoAndIssuedAtBetween(
            long couponNo, int memberNo, LocalDateTime start, LocalDateTime end);

    List<CouponDetails> findByMemberNoAndIsUsedIgnoreCaseOrderByIssuedAtDesc(int memberNo, String isUsed);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from CouponDetails d where d.couponDetailNo = :detailNo and d.memberNo = :memberNo")
    Optional<CouponDetails> findOwnedByIdForUpdate(
            @Param("detailNo") long detailNo, @Param("memberNo") int memberNo);
}
