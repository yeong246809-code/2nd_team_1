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
}