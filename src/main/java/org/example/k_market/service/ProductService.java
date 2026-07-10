package org.example.k_market.service;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Product;
import org.example.k_market.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 번호로 상품 한 건 조회
    public Product findById(Long prodNo) {

        if (prodNo == null) {
            return null;
        }

        return productRepository.findById(prodNo)
                .orElse(null);
    }
}