package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.OrderDetailsDTO;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "order_details")
public class OrderDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Transient
    private String prodName;

    public OrderDetailsDTO toDTO(){
        return OrderDetailsDTO.builder()
                .orderDetailNo(orderDetailNo).orderNo(orderNo).productNo(productNo)
                .shopNo(shopNo).quantity(quantity).price(price)
                .discountPrice(discountPrice).shippingFee(shippingFee)
                .rewardPoints(rewardPoints).status(status)
                .build();
    }
}