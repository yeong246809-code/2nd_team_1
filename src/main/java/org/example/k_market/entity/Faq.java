package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.FaqDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "faq")
public class Faq {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int no;
    private String type1;
    private String type2;
    private String title;
    private String content;
    private int viewCount;
    private int memberNo;
    private LocalDateTime createdAt;

    public FaqDTO toDTO(){
        return FaqDTO.builder()
                .no(no).type1(type1).type2(type2).title(title)
                .content(content).viewCount(viewCount).memberNo(memberNo)
                .createdAt(createdAt)
                .build();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
}