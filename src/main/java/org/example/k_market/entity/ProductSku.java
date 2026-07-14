package org.example.k_market.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_skus")
public class ProductSku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skuNo")
    private Long skuNo;

    @Column(name = "prodNo", nullable = false)
    private Long prodNo;

    @Column(name = "skuName", nullable = false)
    private String skuName;

    @Column(name = "price")
    private Integer price;

    @Column(name = "stock")
    private Integer stock;

    public void decreaseStock(int quantity) {
        int current = stock == null ? 0 : stock;
        if (quantity < 1 || current < quantity) {
            throw new IllegalArgumentException("SKU 재고가 부족합니다.");
        }
        stock = current - quantity;
    }
}
