package org.example.k_market.repository;

import org.example.k_market.entity.ProductOptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionsRepository
        extends JpaRepository<ProductOptions, Long> {

    List<ProductOptions> findByProdNoOrderByOptionNoAsc(long prodNo);

    void deleteByProdNo(long prodNo);
}