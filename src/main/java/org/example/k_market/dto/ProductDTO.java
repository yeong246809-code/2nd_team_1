package org.example.k_market.dto;

import lombok.*;
import org.example.k_market.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductDTO {
    private Long prodNo;             // DAO 및 Entity 규격과 일치 (Long)
    private Integer cateNo;
    private Integer shopNo;
    private String name;
    private String description;
    private String manufacturer;
    private Integer price;
    private Integer discountRate;
    private Integer rewardPoints;
    private Integer stockQuantity;
    private Integer shippingFee;
    private String thumb1;
    private String thumb2;
    private String thumb3;
    private String detailContent;
    private String status;
    private String taxFreeYn;
    private String receiptYn;
    private String bizType;
    private String origin;
    private Integer viewCount;
    private Integer salesCount;
    private BigDecimal rating;
    private LocalDateTime createdAt;

    /**
     * ProductDAO에서 사용하는 DTO -> Entity 변환 메서드
     */
    public Product toEntity() {
        return Product.builder()
                .prodNo(this.prodNo)
                .cateNo(this.cateNo)
                .shopNo(this.shopNo)
                .name(this.name)
                .description(this.description)
                .manufacturer(this.manufacturer)
                .price(this.price)
                .discountRate(this.discountRate)
                .rewardPoints(this.rewardPoints)
                .stockQuantity(this.stockQuantity)
                .shippingFee(this.shippingFee)
                .thumb1(this.thumb1)
                .thumb2(this.thumb2)
                .thumb3(this.thumb3)
                .detailContent(this.detailContent)
                .status(this.status)
                .taxFreeYn(this.taxFreeYn)
                .receiptYn(this.receiptYn)
                .bizType(this.bizType)
                .origin(this.origin)
                .viewCount(this.viewCount)
                .salesCount(this.salesCount)
                .rating(this.rating)
                .createdAt(this.createdAt)
                .build();
    }

    /**
     * 타임리프 화면 출력용 실시간 할인율 적용 가격 계산 메서드
     */
    public int getDiscountPrice() {
        if (this.discountRate == null || this.discountRate == 0) {
            return this.price != null ? this.price : 0;
        }
        return (int) (this.price * (1 - (this.discountRate / 100.0)));
    }
}