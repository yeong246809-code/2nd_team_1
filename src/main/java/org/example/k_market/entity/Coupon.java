package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.CouponDTO;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "coupon")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long couponNo;
    private String issuerName;
    private String couponType;
    private String name;
    private String benefitType;
    private int benefitValue;
    private Integer shopNo;
    private Long prodNo;

    @Transient
    private String dateType;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer validDays;
    private String notes;

    @Setter
    private String status;
    private LocalDateTime createdAt;

    public CouponDTO toDTO(){
        String resolvedDateType = startDate != null || endDate != null ? "PERIOD" : "DAYS";
        return CouponDTO.builder()
                .couponNo(couponNo).issuerName(issuerName).couponType(couponType)
                .name(name).benefitType(benefitType).benefitValue(benefitValue)
                .dateType(resolvedDateType).startDate(startDate).endDate(endDate)
                .validDays(validDays).notes(notes).status(status).createdAt(createdAt)
                .shopNo(shopNo).prodNo(prodNo)
                .build();
    }
}
