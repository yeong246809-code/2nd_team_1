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
    @Column(name = "cartNo")
    private long cartNo;

    @Column(name = "memberNo", nullable = false)
    private int memberNo;

    @Column(name = "productNo", nullable = false)
    private long prodNo;

    @Column(name = "skuNo")
    private Long skuNo;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    public void changeQuantity(int quantity) {
        this.quantity = quantity;
    }

    public CartDTO toDTO(){
        return CartDTO.builder()
                .cartNo(cartNo).memberNo(memberNo).prodNo(prodNo)
                .skuNo(skuNo).quantity(quantity).createdAt(createdAt)
                .build();
    }
}
