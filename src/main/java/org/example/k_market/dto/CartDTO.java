package org.example.k_market.dto;

import org.example.k_market.entity.Cart;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartDTO {

    private long cartNo;
    private int memberNo;
    private long prodNo;
    private Long optItemNo;
    private int quantity;
    private LocalDateTime createdAt;

    public Cart toEntity(){
        return Cart.builder()
                .cartNo(cartNo).memberNo(memberNo).prodNo(prodNo)
                .optItemNo(optItemNo).quantity(quantity).createdAt(createdAt)
                .build();
    }
}