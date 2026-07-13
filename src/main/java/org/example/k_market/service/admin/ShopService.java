package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.PageResponseDTO;
import org.example.k_market.dto.ShopDTO;
import org.example.k_market.entity.Shop;
import org.example.k_market.repository.ShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;

    // 상점 목록 조회 및 검색 필터링
    public PageResponseDTO<ShopDTO> getShopList(String searchType, String keyword, String statusFilter, String sort, int pg) {
        Pageable pageable = PageRequest.of(pg - 1, 5);

        // ★ 방금 우리가 만든 Querydsl 메서드 단 1개만 호출하면 모든 조건과 정렬이 알아서 해결됨!
        Page<Shop> pageResult = shopRepository.searchShops(searchType, keyword, statusFilter, sort, pageable);

        Page<ShopDTO> dtoPage = pageResult.map(Shop::toDTO);
        return new PageResponseDTO<>(dtoPage, 5);
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