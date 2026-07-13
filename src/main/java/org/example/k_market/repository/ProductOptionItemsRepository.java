package org.example.k_market.repository;

import org.example.k_market.entity.ProductOptionItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionItemsRepository
        extends JpaRepository<ProductOptionItems, Long> {

    List<ProductOptionItems> findByOptionNoOrderByOptItemNoAsc(
            long optionNo
    );

    void deleteByOptionNo(long optionNo);
}