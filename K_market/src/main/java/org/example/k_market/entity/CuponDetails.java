package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.CuponDetailsDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cupon_details")
public class CuponDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long cuponDetailNo;
    private long couponNo;
    private int memberNo;
    private String isUsed;
    private LocalDateTime usedAt;
    private LocalDateTime issuedAt;
    private String status;

    public CuponDetailsDTO toDTO(){
        return CuponDetailsDTO.builder()
                .cuponDetailNo(cuponDetailNo).couponNo(couponNo).memberNo(memberNo)
                .isUsed(isUsed).usedAt(usedAt).issuedAt(issuedAt).status(status)
                .build();
    }
}