package org.example.k_market.service.admin;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.*;
import org.example.k_market.entity.Coupon;
import org.example.k_market.entity.Product;
import org.example.k_market.entity.Shop;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.CouponRepository;
import org.example.k_market.repository.ProductRepository;
import org.example.k_market.repository.ShopRepository;
import org.example.k_market.repository.UsersRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final UsersRepository usersRepository;

    /**
     * 1. 쿠폰 목록 조회 (검색 + 페이징)
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<CouponDTO> getCouponList(PageRequestDTO requestDTO) {
        // JPA는 0페이지부터 시작하므로 pg - 1 처리
        int page = (requestDTO.getPg() > 0) ? requestDTO.getPg() - 1 : 0;
        int size = (requestDTO.getSize() > 0) ? requestDTO.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("couponNo").descending());

        // 동적 검색 조건 (Specification) 생성
        Specification<Coupon> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String type = requestDTO.getSearchType();
            String keyword = requestDTO.getKeyword();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String cleanKeyword = keyword.trim();
                if ("couponNo".equals(type)) {
                    try {
                        predicates.add(cb.equal(root.get("couponNo"), Long.parseLong(cleanKeyword)));
                    } catch (NumberFormatException e) {
                        predicates.add(cb.equal(root.get("couponNo"), -1L)); // 숫자가 아니면 빈 결과 유도
                    }
                } else if ("name".equals(type)) {
                    predicates.add(cb.like(root.get("name"), "%" + cleanKeyword + "%"));
                } else if ("issuerName".equals(type)) {
                    predicates.add(cb.like(root.get("issuerName"), "%" + cleanKeyword + "%"));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Coupon> result = couponRepository.findAll(spec, pageable);

        // Entity -> DTO 변환 (발급수/사용수 등은 필요 시 count 쿼리 연동)
        Page<CouponDTO> dtoPage = result.map(Coupon::toDTO);

        // 기존에 쓰시던 PageResponseDTO 생성 (블록 사이즈 5)
        return new PageResponseDTO<>(dtoPage, 5);
    }

    /**
     * 2. 쿠폰 등록 (Security context의 username/id를 기준으로 유저 및 권한 파악)
     */
    public void registerCoupon(CouponDTO dto, String userId) {
        // userId는 MyUserDetails의 getUsername(), 즉 Users의 id 컬럼 값
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        String issuerName = "최고관리자";
        Integer shopNo = null;

        System.out.println("조회된 정보: " + user.getRole());

        // DB의 role 컬럼 값은 "SELLER" 또는 "ADMIN" (MyUserDetails에서 앞에 ROLE_을 붙임)
        if ("SELLER".equalsIgnoreCase(user.getRole())) {
            // SellerShopAccessInterceptor와 동일한 방식으로 memberNo를 통해 Shop 조회
            Shop shop = shopRepository.findByMemberNo(user.getMemberNo())
                    .orElseThrow(() -> new IllegalArgumentException("상점 정보를 찾을 수 없습니다."));
            System.out.println("조회된 상점명: " + shop.getName());
            issuerName = shop.getName();
            shopNo = shop.getShopNo();
        }

        // 할인 금액/율에 따른 benefitType 자동 판별 (예: 50 이하면 할인율(RATE), 초과면 정액(AMOUNT))
        String benefitType = (dto.getBenefitValue() > 0 && dto.getBenefitValue() <= 50) ? "RATE" : "AMOUNT";
        if (dto.getBenefitValue() == 0) benefitType = "FREE_SHIPPING"; // 배송비 무료

        Coupon coupon = Coupon.builder()
                .issuerName(issuerName)
                .couponType(dto.getCouponType())
                .name(dto.getName())
                .benefitType(benefitType)
                .benefitValue(dto.getBenefitValue())
                .dateType(dto.getDateType())
                .startDate("PERIOD".equals(dto.getDateType()) ? dto.getStartDate() : null)
                .endDate("PERIOD".equals(dto.getDateType()) ? dto.getEndDate() : null)
                .validDays("DAYS".equals(dto.getDateType()) ? dto.getValidDays() : null)
                .notes(dto.getNotes())
                .status("ACTIVE") // 초기 상태 (또는 "발급중")
                .createdAt(LocalDateTime.now())
                .shopNo(shopNo)
                .prodNo(dto.getProdNo()) // 선택된 상품 번호 (전체 상품 선택 시 null)
                .build();

        couponRepository.save(coupon);
    }

    /**
     * 3. 로그인한 판매자의 판매 중인 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Product> getSellerProducts(String userId) {
        Users user = usersRepository.findById(userId).orElse(null);
        if (user != null && "SELLER".equalsIgnoreCase(user.getRole())) {
            Shop shop = shopRepository.findById(user.getMemberNo()).orElse(null);
            if (shop != null) {
                return productRepository.findByShopNo(shop.getShopNo());
            }
        }
        return new ArrayList<>();
    }

    /**
     * 4. 쿠폰 발급 종료 처리
     */
    public void stopCoupon(Long couponNo) {
        Coupon coupon = couponRepository.findById(couponNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 쿠폰이 존재하지 않습니다."));
        coupon.setStatus("END"); // 상태를 종료로 변경 (DB 설계에 따라 "종료" 또는 "INACTIVE"로 수정 가능)
    }
}