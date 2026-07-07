package org.example.k_market.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesStatusDTO {
    private int shopNo;               // 번호 (상점 번호)
    private String storeName;         // 상호명
    private String bizNumber;         // 사업자등록번호
    private int orderCount;           // 주문건수
    private int paymentCompleted;     // 결제완료
    private int deliveryInProgress;   // 배송중
    private int deliveryCompleted;    // 배송완료
    private int purchaseConfirmed;    // 구매확정
    private long totalOrderAmount;    // 주문합계 (순수 상품가 * 수량 총합)
    private long totalSalesAmount;    // 매출합계 (할인 등이 반영된 실매출 총합)
}