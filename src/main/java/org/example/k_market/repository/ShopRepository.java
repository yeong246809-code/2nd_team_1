package org.example.k_market.repository;

import org.apache.ibatis.annotations.Param;
import org.example.k_market.entity.Shop;
import org.example.k_market.repository.custom.ShopRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer>, ShopRepositoryCustom {
    Optional<Shop> findByShopNo(Integer shopNo);
    List<Shop> findByStatus(String status);
    Optional<Shop> findByMemberNo(int memberNo);
}
