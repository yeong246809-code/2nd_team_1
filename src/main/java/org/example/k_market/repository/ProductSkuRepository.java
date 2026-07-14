package org.example.k_market.repository;

import org.example.k_market.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {
    List<ProductSku> findByProdNoOrderBySkuNoAsc(Long prodNo);
    Optional<ProductSku> findBySkuNoAndProdNo(Long skuNo, Long prodNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ProductSku s where s.skuNo = :skuNo and s.prodNo = :prodNo")
    Optional<ProductSku> findBySkuNoAndProdNoForUpdate(@Param("skuNo") Long skuNo,
                                                        @Param("prodNo") Long prodNo);
}
