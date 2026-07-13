package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.CmRecruitDTO;
import lombok.*;
import java.time.LocalDate;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cm_recruit")
public class CmRecruit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String department;
    private String experienceType;
    private String employmentType;
    private String title;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDate startDate;
    private LocalDate endDate;

    public CmRecruitDTO toDTO(){
        return CmRecruitDTO.builder()
                .id(id).department(department).experienceType(experienceType)
                .employmentType(employmentType).title(title).status(status)
                .content(content)
                .startDate(startDate).endDate(endDate)
                .build();
    }
}