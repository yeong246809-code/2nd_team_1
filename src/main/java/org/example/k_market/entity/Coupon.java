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
    private String dateType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer validDays;
    private String notes;
    private String status;
    private LocalDateTime createdAt;

    public CouponDTO toDTO(){
        return CouponDTO.builder()
                .couponNo(couponNo).issuerName(issuerName).couponType(couponType)
                .name(name).benefitType(benefitType).benefitValue(benefitValue)
                .dateType(dateType).startDate(startDate).endDate(endDate)
                .validDays(validDays).notes(notes).status(status).createdAt(createdAt)
                .build();
    }
}