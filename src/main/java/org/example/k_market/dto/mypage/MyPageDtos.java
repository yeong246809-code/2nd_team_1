package org.example.k_market.dto.mypage;

import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class MyPageDtos {
    private MyPageDtos() {
    }

    public record Summary(
            long orderCount,
            long couponCount,
            int points,
            long qnaCount
    ) {
    }

    public record OrderItem(
            long orderDetailNo,
            int orderNo,
            Long productNo,
            String productName,
            String thumb,
            String shopName,
            String shopCeo,
            String shopPhone,
            String shopFax,
            String shopEmail,
            String shopBizNumber,
            String shopAddress,
            Integer quantity,
            Integer price,
            Integer totalPrice,
            String status,
            LocalDateTime orderedAt
    ) {
    }

    public record PointItem(
            long pointNo,
            String type,
            Integer amount,
            Integer remainedAmount,
            String description,
            LocalDateTime createdAt,
            LocalDate expiredAt
    ) {
    }

    public record CouponItem(
            long couponDetailNo,
            String couponName,
            String benefitLabel,
            String conditionLabel,
            String statusLabel,
            LocalDate endDate,
            LocalDateTime issuedAt
    ) {
    }

    public record ReviewItem(
            long reviewNo,
            Long productNo,
            String productName,
            Integer rating,
            String content,
            LocalDateTime createdAt
    ) {
    }

    public record QnaItem(
            Integer no,
            String channel,
            String type,
            String title,
            String statusLabel,
            LocalDateTime createdAt
    ) {
    }

    public record Dashboard(
            Summary summary,
            List<OrderItem> recentOrders,
            List<PointItem> recentPoints,
            List<QnaItem> recentQnas
    ) {
    }

    public record PageBlock<T>(
            Page<T> page,
            int currentPage,
            int startPage,
            int endPage
    ) {
    }
}
