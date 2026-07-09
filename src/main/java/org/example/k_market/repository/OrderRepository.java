package org.example.k_market.repository;

import org.example.k_market.entity.Order;
import org.example.k_market.entity.OrderDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// OrderRepository.java
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findByOrderNoContaining(int orderNo, Pageable pageable);
    Page<Order> findAll(Pageable pageable);

    List<OrderDetails> findByOrderNo(int orderNo);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user")
    Page<Order> findAllWithJoin(Pageable pageable);

}
