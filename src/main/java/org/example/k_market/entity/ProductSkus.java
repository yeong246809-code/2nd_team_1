package org.example.k_market.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_skus")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductSkus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer skuNo;

    // 상품 엔티티와 연관 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prodNo", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String skuName; // 블랙/S

    @Column(nullable = true)
    private Integer price;  // 추가 금액

    @Column(nullable = true)
    private Integer stock;  // 재고
}