package org.example.k_market.dto;

import org.example.k_market.entity.OrderDetails;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailsDTO {

    private long orderDetailNo;
    private long orderNo;
    private long productNo;
    private long shopNo;
    private int quantity;
    private int price;
    private int discountPrice;
    private int shippingFee;
    private int rewardPoints;
    private String status;

    private String prodName;

    private int TotalPaymentPrice;

    public OrderDetails toEntity(){
        return OrderDetails.builder()
                .orderDetailNo(orderDetailNo).orderNo(orderNo).productNo(productNo)
                .shopNo(shopNo).quantity(quantity).price(price)
                .discountPrice(discountPrice).shippingFee(shippingFee)
                .rewardPoints(rewardPoints).status(status)
                .build();
    }
}