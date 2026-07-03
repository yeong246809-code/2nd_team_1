package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.OrderDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "`order`")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderNo;
    private int memberNo;
    private String paymentMethod;
    private int totalProductPrice;
    private int totalDiscountPrice;
    private int totalShippingFee;
    private int usedPoints;
    private int totalPaymentPrice;
    private LocalDateTime createdAt;

    public OrderDTO toDTO(){
        return OrderDTO.builder()
                .orderNo(orderNo).memberNo(memberNo).paymentMethod(paymentMethod)
                .totalProductPrice(totalProductPrice).totalDiscountPrice(totalDiscountPrice)
                .totalShippingFee(totalShippingFee).usedPoints(usedPoints)
                .totalPaymentPrice(totalPaymentPrice).createdAt(createdAt)
                .build();
    }
}