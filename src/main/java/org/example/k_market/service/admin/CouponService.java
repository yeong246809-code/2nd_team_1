package org.example.k_market.service.admin;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.*;
import org.example.k_market.entity.*;
import org.example.k_market.repository.*;
import org.example.k_market.service.CouponIssuanceService;
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
    private final CouponDetailsRepository couponDetailsRepository;

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
        Page<CouponDTO> dtoPage = result.map(coupon -> {
            CouponDTO dto = coupon.toDTO();
            dto.setIssueCount(couponDetailsRepository.countByCouponNo(coupon.getCouponNo()));
            dto.setUseCount(couponDetailsRepository.countByCouponNoAndIsUsed(coupon.getCouponNo(), "Y"));
            return dto;
        });

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
        boolean seller = "SELLER".equalsIgnoreCase(user.getRole());

        if ("ADMIN".equalsIgnoreCase(user.getRole())
                && CouponIssuanceService.INDIVIDUAL_PRODUCT.equals(dto.getCouponType())) {
            throw new IllegalArgumentException("관리자는 개별상품 쿠폰을 생성할 수 없습니다.");
        }
        if (seller && !CouponIssuanceService.INDIVIDUAL_PRODUCT.equals(dto.getCouponType())) {
            throw new IllegalArgumentException("판매자는 개별상품 쿠폰만 생성할 수 있습니다.");
        }

        System.out.println("조회된 정보: " + user.getRole());

        // DB의 role 컬럼 값은 "SELLER" 또는 "ADMIN" (MyUserDetails에서 앞에 ROLE_을 붙임)
        if (seller) {
            // SellerShopAccessInterceptor와 동일한 방식으로 memberNo를 통해 Shop 조회
            Shop shop = shopRepository.findByMemberNo(user.getMemberNo())
                    .orElseThrow(() -> new IllegalArgumentException("상점 정보를 찾을 수 없습니다."));
            System.out.println("조회된 상점명: " + shop.getName());
            issuerName = shop.getName();
            shopNo = shop.getShopNo();
            if (dto.getProdNo() == null) throw new IllegalArgumentException("쿠폰을 적용할 상품을 선택해주세요.");
            Product selectedProduct = productRepository.findById(dto.getProdNo())
                    .orElseThrow(() -> new IllegalArgumentException("선택한 상품을 찾을 수 없습니다."));
            if (!shopNo.equals(selectedProduct.getShopNo())) {
                throw new IllegalArgumentException("본인 상점의 상품에만 쿠폰을 생성할 수 있습니다.");
            }
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

    /**
     * 5. 쿠폰 발급 현황 목록 조회 (검색 + 페이징 + 팝업 상세정보 매핑)
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<CouponDetailsDTO> getIssuedCouponList(PageRequestDTO requestDTO) {
        int page = (requestDTO.getPg() > 0) ? requestDTO.getPg() - 1 : 0;
        int size = (requestDTO.getSize() > 0) ? requestDTO.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("couponDetailNo").descending());

        // 동적 검색 조건 (Specification) 생성
        Specification<org.example.k_market.entity.CouponDetails> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String type = requestDTO.getSearchType();
            String keyword = requestDTO.getKeyword();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String cleanKeyword = keyword.trim();
                if ("couponDetailNo".equals(type)) {
                    try {
                        predicates.add(cb.equal(root.get("couponDetailNo"), Long.parseLong(cleanKeyword)));
                    } catch (NumberFormatException e) {
                        predicates.add(cb.equal(root.get("couponDetailNo"), -1L));
                    }
                } else if ("couponNo".equals(type)) {
                    try {
                        predicates.add(cb.equal(root.get("couponNo"), Long.parseLong(cleanKeyword)));
                    } catch (NumberFormatException e) {
                        predicates.add(cb.equal(root.get("couponNo"), -1L));
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // DB 페이징 조회
        Page<org.example.k_market.entity.CouponDetails> result = couponDetailsRepository.findAll(spec, pageable);

        // Entity -> DTO 변환 및 팝업창에 띄울 원본 쿠폰 정보 매핑
        Page<CouponDetailsDTO> dtoPage = result.map(detail -> {
            CouponDetailsDTO dto = detail.toDTO();

            // 1. 원본 쿠폰 정보 매핑 (팝업창용 상세 데이터 전부 포함)
            couponRepository.findById(detail.getCouponNo()).ifPresent(c -> {
                dto.setCouponType(c.getCouponType());
                dto.setCouponName(c.getName());
                dto.setIssuerName(c.getIssuerName()); // 발급처
                dto.setBenefitType(c.getBenefitType()); // 혜택 타입
                dto.setBenefitValue(c.getBenefitValue()); // 혜택 값
                dto.setDateType(c.getStartDate() != null || c.getEndDate() != null ? "PERIOD" : "DAYS");
                dto.setStartDate(c.getStartDate());
                dto.setEndDate(c.getEndDate());
                dto.setValidDays(c.getValidDays());
                dto.setNotes(c.getNotes());               // 유의사항
            });

            // 2. 발급받은 유저의 ID 매핑
            usersRepository.findByMemberNo(detail.getMemberNo()).ifPresent(u -> {
                dto.setUserId(u.getId());
            });
            return dto;
        });

        return new PageResponseDTO<>(dtoPage, 5);
    }

    /**
     * 6. 쿠폰 발급 중단 처리 (비즈니스 메서드 활용)
     */
    public void stopIssuedCoupon(Long couponDetailNo) {
        org.example.k_market.entity.CouponDetails details = couponDetailsRepository.findById(couponDetailNo)
                .orElseThrow(() -> new IllegalArgumentException("발급 내역이 존재하지 않습니다."));

        // 엔티티에 만든 비즈니스 메서드 호출 (앞서 수정했던 stopIssue() 메서드)
        details.stopIssue();
    }
}
