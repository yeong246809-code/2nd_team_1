package org.example.k_market.dto;

import org.example.k_market.entity.CmRecruit;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CmRecruitDTO {

    private int id;
    private String department;
    private String experienceType;
    private String employmentType;
    private String title;
    private String status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String content;

    public CmRecruit toEntity(){
        return CmRecruit.builder()
                .id(id).department(department).experienceType(experienceType)
                .employmentType(employmentType).title(title).status(status)
                .content(content)
                .startDate(startDate).endDate(endDate)
                .build();
    }
}