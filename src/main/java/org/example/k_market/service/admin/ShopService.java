package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.ShopDTO;
import org.example.k_market.entity.Shop;
import org.example.k_market.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;

    // 상점 목록 조회 및 검색 필터링
    public List<ShopDTO> getShopList(String searchType, String keyword) {
        List<Shop> shops;

        if (keyword != null && !keyword.trim().isEmpty()) {
            switch (searchType) {
                case "name":
                    shops = shopRepository.findActiveByName(keyword);
                    break;
                case "ceo":
                    shops = shopRepository.findActiveByCeo(keyword);
                    break;
                case "bizNumber":
                    shops = shopRepository.findActiveByBizNumber(keyword);
                    break;
                case "phone":
                    shops = shopRepository.findActiveByPhone(keyword);
                    break;
                default:
                    shops = shopRepository.findAllActiveShops();
            }
        } else {
            // 검색어가 없으면 전체 활성 상점 조회
            shops = shopRepository.findAllActiveShops();
        }

        // Entity 리스트를 DTO 리스트로 변환하여 반환
        return shops.stream().map(Shop::toDTO).collect(Collectors.toList());
    }

    // 상점 상태 변경 (운영중, 운영중지, 운영준비 등)
    @Transactional
    public void updateShopStatus(Integer memberNo, String status) {
        Shop shop = shopRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상점입니다."));

        shop.updateStatus(status); // 변경 감지(Dirty Checking)로 자동 UPDATE 쿼리 실행
    }

    // 상점 비활
    @Transactional
    public void deleteShops(List<Integer> memberNos) {
        List<Shop> shops = shopRepository.findAllById(memberNos);
        shops.forEach(Shop::delete); // Shop 엔티티의 delete() 호출
    }
}