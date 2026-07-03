package kr.co.k_market.dto;

import kr.co.k_market.entity.CmStory;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CmStoryDTO {

    private int id;
    private String title;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;

    public CmStory toEntity(){
        return CmStory.builder()
                .id(id).title(title).content(content)
                .imageUrl(imageUrl).createdAt(createdAt)
                .build();
    }
}