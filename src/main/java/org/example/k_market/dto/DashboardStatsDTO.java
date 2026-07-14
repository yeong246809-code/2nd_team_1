package org.example.k_market.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    // 1. 운영현황
    private long pendingDeposit;    // 입금대기
    private long preparingDelivery; // 배송준비
    private long cancelRequest;     // 취소요청
    private long exchangeRequest;   // 교환요청
    private long returnRequest;     // 반품요청

    // 2. 주요지표
    private long orderCount;        // 주문건수
    private long orderAmount;       // 주문금액
    private long memberJoinCount;   // 회원가입
    private long visitorCount;      // 방문자수
    private long qnaCount;          // 문의게시글
}