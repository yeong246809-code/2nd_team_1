package org.example.k_market.repository;

import org.example.k_market.entity.OrderClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderClaimRepository extends JpaRepository<OrderClaim, Long> {
    boolean existsByOrderDetailNoAndMemberNoAndType(long orderDetailNo, int memberNo, String type);

    boolean existsByOrderDetailNoAndMemberNoAndTypeAndStatus(long orderDetailNo, int memberNo, String type, String status);

    Optional<OrderClaim> findFirstByOrderDetailNoAndMemberNoAndTypeAndStatusOrderByCreatedAtDesc(
            long orderDetailNo,
            int memberNo,
            String type,
            String status
    );
}
