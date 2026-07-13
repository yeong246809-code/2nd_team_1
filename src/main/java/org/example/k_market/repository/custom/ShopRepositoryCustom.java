package org.example.k_market.repository.custom;

import org.example.k_market.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShopRepositoryCustom {
    // 우리가 방금 만들었던 그 복잡한 쿼리를 대체할 메서드!
    Page<Shop> searchShops(String searchType, String keyword, String statusFilter, String sort, Pageable pageable);
}