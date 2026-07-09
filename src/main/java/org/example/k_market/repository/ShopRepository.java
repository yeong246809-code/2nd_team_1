package org.example.k_market.repository;

import org.apache.ibatis.annotations.Param;
import org.example.k_market.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {

    Optional<Shop> findByShopNo(Integer shopNo);

    @Query("SELECT s FROM Shop s WHERE s.status != 'DELETED' OR s.status IS NULL")
    List<Shop> findAllActiveShops();
    // ✅ 검색할 때도 'DELETE' 상태가 아닌 것만 가져오도록 수정
    @Query("SELECT s FROM Shop s WHERE s.name LIKE %:keyword% AND (s.status != 'DELETED' OR s.status IS NULL)")
    List<Shop> findActiveByName(@Param("keyword") String keyword);

    @Query("SELECT s FROM Shop s WHERE s.ceo LIKE %:keyword% AND (s.status != 'DELETED' OR s.status IS NULL)")
    List<Shop> findActiveByCeo(@Param("keyword") String keyword);

    @Query("SELECT s FROM Shop s WHERE s.bizNumber LIKE %:keyword% AND (s.status != 'DELETED' OR s.status IS NULL)")
    List<Shop> findActiveByBizNumber(@Param("keyword") String keyword);

    @Query("SELECT s FROM Shop s WHERE s.phone LIKE %:keyword% AND (s.status != 'DELETED' OR s.status IS NULL)")
    List<Shop> findActiveByPhone(@Param("keyword") String keyword);
}
