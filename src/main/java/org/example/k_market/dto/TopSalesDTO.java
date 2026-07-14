package org.example.k_market.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSalesDTO {
    private String categoryName; // 카테고리명 (또는 Top3 외엔 "기타")
    private long totalSales;     // 총 매출금액 (원)
    private double percentage;   // 비율 (%) -> 그래프의 도넛 너비 및 텍스트용
}