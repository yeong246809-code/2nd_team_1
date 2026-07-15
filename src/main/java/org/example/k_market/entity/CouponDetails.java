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

    // 🟢 상태 변경을 위한 의미 있는 비즈니스 메서드 추가
    public void stopIssue() {
        this.status = "중단";
    }

    public void use() {
        this.isUsed = "Y";
        this.usedAt = LocalDateTime.now();
        this.status = "사용완료";
    }

    public CouponDetailsDTO toDTO(){
        return CouponDetailsDTO.builder()
                .couponDetailNo(couponDetailNo).couponNo(couponNo).memberNo(memberNo)
                .isUsed(isUsed).usedAt(usedAt).issuedAt(issuedAt).status(status)
                .build();
    }
}
