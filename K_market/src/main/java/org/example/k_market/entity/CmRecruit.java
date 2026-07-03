package kr.co.kmarket.entity;

import jakarta.persistence.*;
import kr.co.kmarket.dto.CmRecruitDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cm_recruit")
public class CmRecruit {
    @Id
    private int id;
    private String department;
    private String experienceType;
    private String employmentType;
    private String title;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public CmRecruitDTO toDTO(){
        return CmRecruitDTO.builder()
                .id(id).department(department).experienceType(experienceType)
                .employmentType(employmentType).title(title).status(status)
                .startDate(startDate).endDate(endDate)
                .build();
    }
}