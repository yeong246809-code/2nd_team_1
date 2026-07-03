package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.VersionDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "version")
public class Version {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String versionCode;
    private int memberNo;
    private LocalDateTime createdAt;
    private String changeLog;

    public VersionDTO toDTO(){
        return VersionDTO.builder()
                .id(id).versionCode(versionCode).memberNo(memberNo)
                .createdAt(createdAt).changeLog(changeLog)
                .build();
    }
}