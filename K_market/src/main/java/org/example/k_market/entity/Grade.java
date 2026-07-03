package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.GradeDTO;
import lombok.*;
import java.math.BigDecimal;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "grade")
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int gradeNo;
    private String name;
    private BigDecimal rewardRate;

    public GradeDTO toDTO(){
        return GradeDTO.builder()
                .gradeNo(gradeNo).name(name).rewardRate(rewardRate)
                .build();
    }
}