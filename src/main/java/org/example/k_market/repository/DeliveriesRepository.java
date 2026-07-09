package org.example.k_market.repository;

import org.example.k_market.entity.Deliveries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DeliveriesRepository extends JpaRepository<Deliveries, Long> {
    // 주문 번호로 배송 정보를 찾기 위한 메서드 추가
    Optional<Deliveries> findByOrderNo(int orderNo);
}