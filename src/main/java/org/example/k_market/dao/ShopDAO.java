package org.example.k_market.dao;

import org.example.k_market.dto.ShopDTO;
import org.example.k_market.entity.Shop;
import org.example.k_market.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ShopDAO {

    private final ShopRepository shopRepository;

    public ShopDTO save(ShopDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        Shop entity = dto.toEntity();
        Shop savedEntity = shopRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<ShopDTO> findById(int shopNo) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return shopRepository.findById(shopNo)
                .map(Shop::toDTO);
    }

    public List<ShopDTO> findAll() {
        return shopRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(Shop::toDTO)
                .toList();
    }

    public void deleteById(int shopNo) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (shopRepository.existsById(shopNo)) {
            shopRepository.deleteById(shopNo);
        }
    }
}
