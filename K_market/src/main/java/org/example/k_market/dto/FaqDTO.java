package org.example.k_market.dto;

import org.example.k_market.entity.Faq;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FaqDTO {

    private int no;
    private String type1;
    private String type2;
    private String title;
    private String content;
    private int viewCount;
    private int memberNo;
    private LocalDateTime createdAt;

    public Faq toEntity(){
        return Faq.builder()
                .no(no).type1(type1).type2(type2).title(title)
                .content(content).viewCount(viewCount).memberNo(memberNo)
                .createdAt(createdAt)
                .build();
    }
}