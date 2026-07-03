package kr.co.k_market.dto;

import kr.co.k_market.entity.CmRecruit;
import lombok.*;
import java.time.LocalDateTime;

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
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public CmRecruit toEntity(){
        return CmRecruit.builder()
                .id(id).department(department).experienceType(experienceType)
                .employmentType(employmentType).title(title).status(status)
                .startDate(startDate).endDate(endDate)
                .build();
    }
}