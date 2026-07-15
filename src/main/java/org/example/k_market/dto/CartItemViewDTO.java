package org.example.k_market.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class CartItemViewDTO {
    private String itemKey;
    private long cartNo;
    private long prodNo;
    private long shopNo;
    private Long skuNo;
    private String skuName;
    private String name;
    private String description;
    private String thumb1;
    private int quantity;
    private int unitPrice;
    private int discountRate;
    private int rewardPoints;
    private int shippingFee;
    private int lineTotal;
    private int lineRewardPoints;
    private int maxQuantity;
    private boolean soldOut;
}
