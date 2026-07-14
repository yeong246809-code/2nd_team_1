package org.example.k_market.repository;

import org.example.k_market.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCateNo(Integer cateNo);

    List<Product> findByCateNoIn(List<Integer> cateNos);

    List<Product> findByCateNoIn(List<Integer> cateNos, Sort sort);

    Page<Product> findByCateNoIn(List<Integer> cateNos, Pageable pageable);

    List<Product> findTop3ByOrderBySalesCountDesc();
}