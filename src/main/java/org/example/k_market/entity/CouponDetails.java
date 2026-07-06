package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.CouponDetailsDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "coupon_details")
public class CouponDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long couponDetailNo;
    private long couponNo;
    private int memberNo;
    private String isUsed;
    private LocalDateTime usedAt;
    private LocalDateTime issuedAt;
    private String status;

    public CouponDetailsDTO toDTO(){
        return CouponDetailsDTO.builder()
                .couponDetailNo(couponDetailNo).couponNo(couponNo).memberNo(memberNo)
                .isUsed(isUsed).usedAt(usedAt).issuedAt(issuedAt).status(status)
                .build();
    }
}