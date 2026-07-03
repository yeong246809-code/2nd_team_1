package kr.co.k_market.dto;

import kr.co.k_market.entity.Product;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {

    private long prodNo;
    private int cateNo;
    private long shopNo;
    private String name;
    private String description;
    private String manufacturer;
    private int price;
    private int discountRate;
    private int rewardPoints;
    private int stockQuantity;
    private int shippingFee;
    private String thumb1;
    private String thumb2;
    private String thumb3;
    private String detailContent;
    private String status;
    private String taxFreeYn;
    private String receiptYn;
    private String bizType;
    private String origin;
    private int viewCount;
    private int salesCount;
    private BigDecimal rating;
    private LocalDateTime createdAt;

    public Product toEntity(){
        return Product.builder()
                .prodNo(prodNo).cateNo(cateNo).shopNo(shopNo).name(name)
                .description(description).manufacturer(manufacturer).price(price)
                .discountRate(discountRate).rewardPoints(rewardPoints).stockQuantity(stockQuantity)
                .shippingFee(shippingFee).thumb1(thumb1).thumb2(thumb2).thumb3(thumb3)
                .detailContent(detailContent).status(status).taxFreeYn(taxFreeYn)
                .receiptYn(receiptYn).bizType(bizType).origin(origin).viewCount(viewCount)
                .salesCount(salesCount).rating(rating).createdAt(createdAt)
                .build();
    }
}