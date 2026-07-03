package org.example.k_market.dto;

import org.example.k_market.entity.CuponDetails;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CuponDetailsDTO {

    private long cuponDetailNo;
    private long couponNo;
    private int memberNo;
    private String isUsed;
    private LocalDateTime usedAt;
    private LocalDateTime issuedAt;
    private String status;

    public CuponDetails toEntity(){
        return CuponDetails.builder()
                .cuponDetailNo(cuponDetailNo).couponNo(couponNo).memberNo(memberNo)
                .isUsed(isUsed).usedAt(usedAt).issuedAt(issuedAt).status(status)
                .build();
    }
}