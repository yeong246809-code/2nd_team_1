package org.example.k_market.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.k_market.entity.Order;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {

    private int orderNo; // 엔티티와 동일하게 long -> int로 변경!
    private String id;
    private int memberNo;
    private String orderName;
    private String paymentMethod;
    private int totalProductPrice;
    private int totalDiscountPrice;
    private int totalShippingFee;
    private int usedPoints;
    private int totalPaymentPrice;
    private LocalDateTime createdAt;

    private String memberName;

    private String status;

    private String recipientName; // 수취인
    private String recipientPhone; // 수취인 전화번호
    private String baseAddress; // 배송지 주소

    private int prodNo; // 상품번호
    private String name; // 상품명
    private int price; // 가격
    private int discountRate; // 할인
    private int stockQuantity; // 수량

    private List<OrderDetailsDTO> orderItems;


    public Order toEntity(){
        return Order.builder()
                .orderNo(orderNo)
                .memberNo(memberNo)
                .orderName(orderName)
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