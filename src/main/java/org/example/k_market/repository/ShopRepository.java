package org.example.k_market.repository;

import org.example.k_market.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {
    Optional<Shop> findByShopNo(Integer shopNo);
    List<Shop> findByNameContaining(String keyword);
    List<Shop> findByCeoContaining(String keyword);
    List<Shop> findByBizNumberContaining(String keyword);
    List<Shop> findByPhoneContaining(String keyword);
}
