package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.CartDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long cartNo;
    private int memberNo;
    private long prodNo;
    private Long optItemNo;
    private int quantity;
    private LocalDateTime createdAt;

    public CartDTO toDTO(){
        return CartDTO.builder()
                .cartNo(cartNo).memberNo(memberNo).prodNo(prodNo)
                .optItemNo(optItemNo).quantity(quantity).createdAt(createdAt)
                .build();
    }
}