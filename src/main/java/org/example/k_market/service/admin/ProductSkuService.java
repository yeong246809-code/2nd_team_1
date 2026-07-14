package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.ProductSkus;
import org.example.k_market.repository.ProductSkuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductSkuService {

    private final ProductSkuRepository productSkuRepository;

    // 상품 등록 시 SKU 저장
    public void saveAll(List<ProductSkus> skuList) {
        productSkuRepository.saveAll(skuList);
    }

    public void replaceSkus(Long prodNo, List<ProductSkus> newSkuList) {
        productSkuRepository.deleteByProduct_ProdNo(prodNo);
        productSkuRepository.saveAll(newSkuList);
    }

    @Transactional(readOnly = true)
    public List<ProductSkus> findByProdNo(Long prodNo) {
        return productSkuRepository.findByProduct_ProdNo(prodNo);
    }
}