package org.example.k_market.repository;

import org.example.k_market.entity.OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {
    List<OrderDelivery> findByOrderNoOrderByShipmentIndexAscOrderDeliveryNoAsc(long orderNo);
}
