package org.example.k_market.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesStatusDTO {

    // 1. 상점 기본 정보 (shop 테이블 기반)
    private long shopNo;              // 상점번호
    private String storeName;         // 상호명
    private String bizNumber;         // 사업자등록번호

    // 2. 주문 및 배송 상태별 카운트 (order_details 및 deliveries 테이블 통계)
    private int orderCount;           // 주문건수
    private int paymentCompleted;     // 결제완료 건수
    private int deliveryInProgress;    // 배송중 건수
    private int deliveryCompleted;    // 배송완료 건수
    private int purchaseConfirmed;    // 구매확정 건수

    // 3. 금액 합계 데이터 (주문금액 및 실제 매출액 연산)
    private long totalOrderAmount;    // 주문합계 (수량 * 단가)
    private long totalSalesAmount;    // 매출합계 (주문금액 - 할인 + 배송비)
}