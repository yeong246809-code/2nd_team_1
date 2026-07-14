package org.example.k_market.repository;

import org.example.k_market.entity.ProductSkus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 관리자 상품 옵션 화면에서 사용하는 연관관계 기반 SKU 저장소.
 * 결제/재고 잠금용 ProductSkuRepository와 이름을 분리한다.
 */
public interface ProductSkusRepository extends JpaRepository<ProductSkus, Integer> {
    List<ProductSkus> findByProduct_ProdNo(Long prodNo);

    void deleteByProduct_ProdNo(Long prodNo);
}
