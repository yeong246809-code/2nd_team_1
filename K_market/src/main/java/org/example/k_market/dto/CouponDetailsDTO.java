package org.example.k_market.dto;

import org.example.k_market.entity.CouponDetails;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponDetailsDTO {

    private long couponDetailNo;
    private long couponNo;
    private int memberNo;
    private String isUsed;
    private LocalDateTime usedAt;
    private LocalDateTime issuedAt;
    private String status;

    public CouponDetails toEntity(){
        return CouponDetails.builder()
                .couponDetailNo(couponDetailNo).couponNo(couponNo).memberNo(memberNo)
                .isUsed(isUsed).usedAt(usedAt).issuedAt(issuedAt).status(status)
                .build();
    }
}