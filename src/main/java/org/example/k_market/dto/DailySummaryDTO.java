package org.example.k_market.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySummaryDTO {
    private String date;        // 날짜 (예: "07-13")
    private long orderCount;    // 주문 건수
    private long payCount;      // 결제 완료 건수
    private long cancelCount;   // 취소 건수
}