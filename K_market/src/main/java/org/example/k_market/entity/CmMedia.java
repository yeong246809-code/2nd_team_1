package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.CmMediaDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cm_media")
public class CmMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String youtubeUrl;
    private String title;
    private String subtitle;
    private LocalDateTime createdAt;

    public CmMediaDTO toDTO(){
        return CmMediaDTO.builder()
                .id(id).youtubeUrl(youtubeUrl).title(title)
                .subtitle(subtitle).createdAt(createdAt)
                .build();
    }
}