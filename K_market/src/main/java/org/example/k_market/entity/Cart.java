package kr.co.kmarket.entity;

import jakarta.persistence.*;
import kr.co.kmarket.dto.CartDTO;
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
    private long productNo;
    private Long optItemNo;
    private int quantity;
    private LocalDateTime createdAt;

    public CartDTO toDTO(){
        return CartDTO.builder()
                .cartNo(cartNo).memberNo(memberNo).productNo(productNo)
                .optItemNo(optItemNo).quantity(quantity).createdAt(createdAt)
                .build();
    }
}