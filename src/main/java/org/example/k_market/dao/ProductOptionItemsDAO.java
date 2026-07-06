package org.example.k_market.dao;

import org.example.k_market.dto.ProductOptionItemsDTO;
import org.example.k_market.entity.ProductOptionItems;
import org.example.k_market.repository.ProductOptionItemsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductOptionItemsDAO {

    private final ProductOptionItemsRepository productOptionItemsRepository;

    public ProductOptionItemsDTO save(ProductOptionItemsDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        ProductOptionItems entity = dto.toEntity();
        ProductOptionItems savedEntity = productOptionItemsRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<ProductOptionItemsDTO> findById(Long optItemNo) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return productOptionItemsRepository.findById(optItemNo)
                .map(ProductOptionItems::toDTO);
    }

    public List<ProductOptionItemsDTO> findAll() {
        return productOptionItemsRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(ProductOptionItems::toDTO)
                .toList();
    }

    public void deleteById(Long optItemNo) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (productOptionItemsRepository.existsById(optItemNo)) {
            productOptionItemsRepository.deleteById(optItemNo);
        }
    }
}
