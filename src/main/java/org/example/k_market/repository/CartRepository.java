package org.example.k_market.repository;

import org.example.k_market.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByMemberNoOrderByCreatedAtDesc(int memberNo);

    @Query("select coalesce(sum(c.quantity), 0) from Cart c where c.memberNo = :memberNo")
    long sumQuantityByMemberNo(@Param("memberNo") int memberNo);

    Optional<Cart> findByCartNoAndMemberNo(long cartNo, int memberNo);

    List<Cart> findByMemberNoAndCartNoIn(int memberNo, List<Long> cartNos);

    @Query("""
            select c from Cart c
            where c.memberNo = :memberNo
              and c.prodNo = :prodNo
              and ((:skuNo is null and c.skuNo is null) or c.skuNo = :skuNo)
            """)
    Optional<Cart> findMatchingItem(@Param("memberNo") int memberNo,
                                    @Param("prodNo") long prodNo,
                                    @Param("skuNo") Long skuNo);
}
