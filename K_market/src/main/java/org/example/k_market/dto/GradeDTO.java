package kr.co.k_market.dto;

import kr.co.k_market.entity.Grade;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GradeDTO {

    private int gradeNo;
    private String name;
    private BigDecimal rewardRate;

    public Grade toEntity(){
        return Grade.builder()
                .gradeNo(gradeNo).name(name).rewardRate(rewardRate)
                .build();
    }
}