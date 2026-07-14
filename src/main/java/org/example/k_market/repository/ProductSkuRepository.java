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
import org.example.k_market.entity.ProductSkus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductSkuRepository extends JpaRepository<ProductSkus, Integer> {

    List<ProductSkus> findByProduct_ProdNo(Long prodNo);
    void deleteByProduct_ProdNo(Long prodNo);
}
