package org.example.k_market.dto;

import org.example.k_market.entity.Order;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO {

    private int orderNo; // 엔티티와 동일하게 long -> int로 변경!
    private String id;
    private int memberNo;
    private String paymentMethod;
    private int totalProductPrice;
    private int totalDiscountPrice;
    private int totalShippingFee;
    private int usedPoints;
    private int totalPaymentPrice;
    private LocalDateTime createdAt;

    private String memberName;

    private String status;

    public Order toEntity(){
        return Order.builder()
                .orderNo(orderNo)
                .memberNo(memberNo)
                .paymentMethod(paymentMethod)
                .totalProductPrice(totalProductPrice)
                .totalDiscountPrice(totalDiscountPrice)
                .totalShippingFee(totalShippingFee)
                .usedPoints(usedPoints)
                .totalPaymentPrice(totalPaymentPrice)
                .createdAt(createdAt)
                .status(status)
                .build();
    }
}