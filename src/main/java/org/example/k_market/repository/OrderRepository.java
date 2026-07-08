package org.example.k_market.repository;

import org.example.k_market.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findByOrderNoContaining(int orderNo, Pageable pageable);

    Page<Order> findAll(Pageable pageable);

    // Member와 Join하여 데이터를 가져오는 JPQL
    @Query("SELECT o FROM Order o JOIN FETCH o.memberNo")
    Page<Order> findAllWithMember(Pageable pageable);

    List<Order> orderNo(int orderNo);
}
