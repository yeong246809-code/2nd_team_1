package org.example.k_market.repository;

import org.example.k_market.entity.ProductSkus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductSkuRepository extends JpaRepository<ProductSkus, Integer> {

    List<ProductSkus> findByProduct_ProdNo(Long prodNo);
    void deleteByProduct_ProdNo(Long prodNo);
}