package org.example.k_market.repository;

import org.example.k_market.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository
        extends JpaRepository<Category, Integer> {

    /**
     * 1차 카테고리 조회
     * parentNo가 null인 카테고리만 가져온다.
     */
    List<Category> findByParentNoIsNull();

    /**
     * 특정 1차 카테고리에 속한 2차 카테고리 조회
     */
    List<Category> findByParentNoOrderByCateNoAsc(
            Integer parentNo
    );
}