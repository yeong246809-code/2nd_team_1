package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.NoticeDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "notice")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int no;
    private String type;
    private String title;
    private String content;
    private int viewCount;
    private int memberNo;
    private LocalDateTime createdAt;

    public NoticeDTO toDTO(){
        return NoticeDTO.builder()
                .no(no).type(type).title(title).content(content)
                .viewCount(viewCount).memberNo(memberNo).createdAt(createdAt)
                .build();
    }
}