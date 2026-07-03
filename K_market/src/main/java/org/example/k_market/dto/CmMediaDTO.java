package kr.co.k_market.dto;

import kr.co.k_market.entity.CmMedia;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CmMediaDTO {

    private int id;
    private String youtubeUrl;
    private String title;
    private String subtitle;
    private LocalDateTime createdAt;

    public CmMedia toEntity(){
        return CmMedia.builder()
                .id(id).youtubeUrl(youtubeUrl).title(title)
                .subtitle(subtitle).createdAt(createdAt)
                .build();
    }
}