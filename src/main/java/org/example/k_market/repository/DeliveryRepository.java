package org.example.k_market.repository;

import org.example.k_market.entity.Deliveries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Deliveries, Long> {

    // 필요하다면 주문 번호로 배송 정보를 찾는 메서드를 추가할 수 있습니다.
    Optional<Deliveries> findByOrderNo(long orderNo);
}