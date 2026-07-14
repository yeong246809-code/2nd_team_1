package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.mypage.MyPageDtos;
import org.example.k_market.entity.*;
import org.example.k_market.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {
    public static final int PAGE_SIZE = 6;
    private static final int PAGE_BLOCK_SIZE = 5;

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final CouponRepository couponRepository;
    private final CouponDetailsRepository couponDetailsRepository;
    private final ReviewRepository reviewRepository;
    private final QnaRepository qnaRepository;
    private final OrderClaimRepository orderClaimRepository;

    public MyPageDtos.Dashboard dashboard(int memberNo) {
        return new MyPageDtos.Dashboard(
                summary(memberNo),
                orders(memberNo, 1).page().getContent(),
                points(memberNo, 1).page().getContent(),
                qnas(memberNo, 1).page().getContent()
        );
    }

    public MyPageDtos.Summary summary(int memberNo) {
        int points = memberRepository.findById(memberNo)
                .map(Member::getPoints)
                .orElse(0);
        return new MyPageDtos.Summary(
                orderDetailsRepository.countByMemberNo(memberNo),
                couponDetailsRepository.countByMemberNoAndIsUsedIgnoreCase(memberNo, "N"),
                points,
                qnaRepository.countByMemberNoAndParentNo(memberNo, 0)
        );
    }

    public MyPageDtos.PageBlock<MyPageDtos.OrderItem> orders(int memberNo, int page) {
        return orders(memberNo, page, null, null);
    }

    public MyPageDtos.PageBlock<MyPageDtos.OrderItem> orders(int memberNo, int page, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = pageRequest(page);
        LocalDateTime startDateTime = startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate == null ? null : endDate.plusDays(1).atStartOfDay();
        Page<OrderDetails> details = startDateTime == null && endDateTime == null
                ? orderDetailsRepository.findByMemberNo(memberNo, pageable)
                : orderDetailsRepository.findByMemberNoAndCreatedAtBetween(memberNo, startDateTime, endDateTime, pageable);
        Page<MyPageDtos.OrderItem> mapped = details
                .map(this::toOrderItem);
        return block(mapped);
    }

    public MyPageDtos.PageBlock<MyPageDtos.PointItem> points(int memberNo, int page) {
        return points(memberNo, page, null, null);
    }

    public MyPageDtos.PageBlock<MyPageDtos.PointItem> points(int memberNo, int page, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = pageRequest(page);
        LocalDateTime startDateTime = startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate == null ? null : endDate.plusDays(1).atStartOfDay();
        Page<PointHistory> points = startDateTime == null && endDateTime == null
                ? pointHistoryRepository.findByMemberNoOrderByCreatedAtDesc(memberNo, pageable)
                : pointHistoryRepository.findByMemberNoAndCreatedAtBetween(memberNo, startDateTime, endDateTime, pageable);
        Page<MyPageDtos.PointItem> mapped = points
                .map(this::toPointItem);
        return block(mapped);
    }

    public MyPageDtos.PageBlock<MyPageDtos.CouponItem> coupons(int memberNo, int page) {
        Page<CouponDetails> details = couponDetailsRepository.findByMemberNoOrderByIssuedAtDesc(memberNo, pageRequest(page));
        List<MyPageDtos.CouponItem> items = details.getContent().stream()
                .map(this::toCouponItem)
                .toList();
        return block(new PageImpl<>(items, details.getPageable(), details.getTotalElements()));
    }

    public MyPageDtos.PageBlock<MyPageDtos.ReviewItem> reviews(int memberNo, int page) {
        Page<MyPageDtos.ReviewItem> mapped = reviewRepository.findByMemberNoOrderByCreatedAtDesc(memberNo, pageRequest(page))
                .map(this::toReviewItem);
        return block(mapped);
    }

    public MyPageDtos.PageBlock<MyPageDtos.ReviewItem> sellerReviews(int shopNo, int page) {
        Page<MyPageDtos.ReviewItem> mapped = reviewRepository
                .findSellerProductReviews(shopNo, pageRequest(page))
                .map(this::toReviewItem);
        return block(mapped);
    }

    public MyPageDtos.PageBlock<MyPageDtos.QnaItem> qnas(int memberNo, int page) {
        Page<MyPageDtos.QnaItem> mapped = qnaRepository.findByMemberNoAndParentNoOrderByNoDesc(memberNo, 0, pageRequest(page))
                .map(this::toQnaItem);
        return block(mapped);
    }

    @Transactional
    public void confirmOrder(long orderDetailNo, int memberNo) {
        OrderDetails detail = requireOwnedOrderDetail(orderDetailNo, memberNo);
        orderDetailsRepository.updateStatus(detail.getOrderDetailNo(), "구매확정");
    }

    @Transactional
    public void claimOrder(long orderDetailNo, int memberNo, String type, String reasonType, String reasonDetail, String attachedImage) {
        requireOwnedOrderDetail(orderDetailNo, memberNo);
        String safeType = "exchange".equalsIgnoreCase(type) ? "교환" : "반품";
        if (orderClaimRepository.existsByOrderDetailNoAndMemberNoAndTypeAndStatus(orderDetailNo, memberNo, safeType, "접수")) {
            return;
        }
        OrderClaim claim = OrderClaim.builder()
                .orderDetailNo(orderDetailNo)
                .memberNo(memberNo)
                .type(safeType)
                .reasonType(valueOr(reasonType, "사유 미선택"))
                .reasonDetail(valueOr(reasonDetail, ""))
                .attachedImage(valueOr(attachedImage, ""))
                .status("접수")
                .createdAt(LocalDateTime.now())
                .build();
        orderClaimRepository.save(claim);
        orderDetailsRepository.updateStatus(orderDetailNo, safeType + "신청");
    }

    @Transactional
    public void cancelReturnClaim(long orderDetailNo, int memberNo) {
        requireOwnedOrderDetail(orderDetailNo, memberNo);
        OrderClaim claim = orderClaimRepository
                .findFirstByOrderDetailNoAndMemberNoAndTypeAndStatusOrderByCreatedAtDesc(orderDetailNo, memberNo, "반품", "접수")
                .orElseThrow(() -> new IllegalArgumentException("취소할 반품 신청이 없습니다."));
        claim.cancel();
        orderDetailsRepository.updateStatus(orderDetailNo, "구매확정");
    }

    @Transactional
    public void deleteReview(long reviewNo, int memberNo) {
        reviewRepository.findById(reviewNo)
                .filter(review -> review.getMemberNo() == memberNo)
                .ifPresent(reviewRepository::delete);
    }

    private OrderDetails requireOwnedOrderDetail(long orderDetailNo, int memberNo) {
        OrderDetails detail = orderDetailsRepository.findById(orderDetailNo)
                .orElseThrow(() -> new IllegalArgumentException("주문 상품을 찾을 수 없습니다."));
        Order order = orderRepository.findById((int) detail.getOrderNo())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        if (order.getMemberNo() != memberNo) {
            throw new IllegalArgumentException("본인의 주문만 처리할 수 있습니다.");
        }
        return detail;
    }

    private MyPageDtos.OrderItem toOrderItem(OrderDetails detail) {
        Order order = orderRepository.findById((int) detail.getOrderNo()).orElse(null);
        Product product = productRepository.findById(detail.getProductNo()).orElse(null);
        Shop shop = detail.getShopNo() > 0 ? shopRepository.findByShopNo((int) detail.getShopNo()).orElse(null) : null;
        int totalPrice = (detail.getPrice() * detail.getQuantity()) - detail.getDiscountPrice() + detail.getShippingFee();

        return new MyPageDtos.OrderItem(
                detail.getOrderDetailNo(),
                (int) detail.getOrderNo(),
                detail.getProductNo(),
                product == null ? "상품 정보 없음" : product.getName(),
                product == null ? null : product.getThumb1(),
                shop == null ? "상호명 없음" : shop.getName(),
                shop == null ? "-" : shop.getCeo(),
                shop == null ? "-" : shop.getPhone(),
                shop == null ? "-" : shop.getFax(),
                "-",
                shop == null ? "-" : shop.getBizNumber(),
                shop == null ? "-" : joinAddress(shop.getZipCode(), shop.getBaseAddress(), shop.getDetailAddress()),
                detail.getQuantity(),
                detail.getPrice(),
                Math.max(totalPrice, 0),
                valueOr(detail.getStatus(), order == null ? "주문완료" : order.getStatus()),
                order == null ? null : order.getCreatedAt()
        );
    }

    private MyPageDtos.PointItem toPointItem(PointHistory point) {
        return new MyPageDtos.PointItem(
                point.getPointNo(),
                point.getAmount() < 0 ? "사용" : "적립",
                point.getAmount(),
                point.getRemainedAmount(),
                valueOr(point.getDescription(), "-"),
                point.getCreatedAt(),
                point.getExpiredAt()
        );
    }

    private MyPageDtos.CouponItem toCouponItem(CouponDetails detail) {
        CouponRepository.CouponSummary coupon = couponRepository.findSummaryByCouponNo(detail.getCouponNo()).orElse(null);
        String used = valueOr(detail.getIsUsed(), "N");
        String status = "Y".equalsIgnoreCase(used) ? "사용완료" : valueOr(detail.getStatus(), "사용가능");
        return new MyPageDtos.CouponItem(
                detail.getCouponDetailNo(),
                coupon == null ? "쿠폰 정보 없음" : coupon.getName(),
                coupon == null ? "-" : benefitLabel(coupon.getBenefitType(), coupon.getBenefitValue()),
                coupon == null ? "-" : valueOr(coupon.getNotes(), "제한조건 없음"),
                status,
                coupon == null ? null : coupon.getEndDate(),
                detail.getIssuedAt()
        );
    }

    private MyPageDtos.ReviewItem toReviewItem(Review review) {
        Product product = productRepository.findById(review.getProdNo()).orElse(null);
        return new MyPageDtos.ReviewItem(
                review.getReviewNO(),
                review.getProdNo(),
                product == null ? "상품 정보 없음" : product.getName(),
                review.getRating(),
                valueOr(review.getContent(), ""),
                review.getCreatedAt()
        );
    }

    private MyPageDtos.QnaItem toQnaItem(Qna qna) {
        String status = "답변완료".equals(qna.getIsAnswered()) || qnaRepository.findByParentNo(qna.getNo()).isPresent()
                ? "답변완료"
                : "검토중";
        return new MyPageDtos.QnaItem(
                qna.getNo(),
                qna.getProdNo() == null ? "고객센터" : "상품문의",
                valueOr(qna.getType1(), valueOr(qna.getType2(), "-")),
                valueOr(qna.getTitle(), "-"),
                status,
                qna.getCreatedAt()
        );
    }

    private Pageable pageRequest(int page) {
        return PageRequest.of(Math.max(page, 1) - 1, PAGE_SIZE);
    }

    private <T> MyPageDtos.PageBlock<T> block(Page<T> page) {
        int current = page.getNumber() + 1;
        int totalPages = Math.max(page.getTotalPages(), 1);
        int start = ((current - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
        int end = Math.min(start + PAGE_BLOCK_SIZE - 1, totalPages);
        return new MyPageDtos.PageBlock<>(page, current, start, end);
    }

    private String benefitLabel(String benefitType, Integer benefitValue) {
        String type = valueOr(benefitType, "");
        int value = benefitValue == null ? 0 : benefitValue;
        if (type.contains("%") || type.equalsIgnoreCase("RATE") || type.contains("율")) {
            return value + "%";
        }
        return String.format("%,d원", value);
    }

    private String joinAddress(String zipCode, String baseAddress, String detailAddress) {
        String zip = zipCode == null || zipCode.isBlank() ? "" : "[" + zipCode + "] ";
        return (zip + valueOr(baseAddress, "") + " " + valueOr(detailAddress, "")).trim();
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
