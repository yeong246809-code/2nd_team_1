package org.example.k_market.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.k_market.dto.ProductDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prodNo;             // DAO 규격에 맞춰 Long으로 설정 (int(11) 수용 가능)

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

    @Column(columnDefinition = "TEXT")
    private String detailContent;

    private String status;
    private String taxFreeYn;
    private String receiptYn;
    private String bizType;
    private String origin;
    private Integer viewCount;
    private Integer salesCount;

    @Column(precision = 38, scale = 2)
    private BigDecimal rating;

    private LocalDateTime createdAt;

    /**
     * ProductDAO에서 사용하는 Entity -> DTO 변환 메서드
     */
    public ProductDTO toDTO() {
        return ProductDTO.builder()
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
}