package org.example.k_market.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageRequestDTO {
    @Builder.Default
    private int pg = 1;
    @Builder.Default
    private int size = 10;

    private String searchType; // "couponNo", "name", "issuerName"
    private String keyword;

    // 공통 페이지네이션 프래그먼트가 사용하는 page 파라미터를 pg와 연결한다.
    public void setPage(int page) {
        this.pg = page;
    }
}
