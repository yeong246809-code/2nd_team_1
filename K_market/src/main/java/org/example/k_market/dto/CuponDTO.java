package org.example.k_market.dto;

import org.example.k_market.entity.Cupon;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CuponDTO {

    private long cuponNo;
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

    public Cupon toEntity(){
        return Cupon.builder()
                .cuponNo(cuponNo).issuerName(issuerName).couponType(couponType)
                .name(name).benefitType(benefitType).benefitValue(benefitValue)
                .dateType(dateType).startDate(startDate).endDate(endDate)
                .validDays(validDays).notes(notes).status(status).createdAt(createdAt)
                .build();
    }
}