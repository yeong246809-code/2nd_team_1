package org.example.k_market.repository;

import org.example.k_market.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByMemberNoOrderByCreatedAtDesc(int memberNo);

    @Query("select coalesce(sum(c.quantity), 0) from Cart c where c.memberNo = :memberNo")
    long sumQuantityByMemberNo(@Param("memberNo") int memberNo);
}
