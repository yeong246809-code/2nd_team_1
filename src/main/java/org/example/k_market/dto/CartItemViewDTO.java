package org.example.k_market.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartItemViewDTO {
    private long cartNo;
    private long prodNo;
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
}
