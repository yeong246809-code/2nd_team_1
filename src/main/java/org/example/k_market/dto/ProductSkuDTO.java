package org.example.k_market.dto;

import lombok.*;
import org.example.k_market.entity.ProductSkus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSkuDTO {
    private Integer skuNo;
    private long prodNo;
    private String skuName;
    private Integer price;
    private Integer stock;

    public ProductSkus toEntity() {
        return ProductSkus.builder()
                .skuNo(this.skuNo)
                .skuName(this.skuName)
                .price(this.price)
                .stock(this.stock)
                .build();
    }
}