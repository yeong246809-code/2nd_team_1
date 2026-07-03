package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.CmStoryDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cm_story")
public class CmStory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;

    public CmStoryDTO toDTO(){
        return CmStoryDTO.builder()
                .id(id).title(title).content(content)
                .imageUrl(imageUrl).createdAt(createdAt)
                .build();
    }
}