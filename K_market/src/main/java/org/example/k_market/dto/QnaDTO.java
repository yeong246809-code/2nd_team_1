package kr.co.k_market.dto;

import kr.co.k_market.entity.Qna;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QnaDTO {

    private int no;
    private String type1;
    private String type2;
    private String title;
    private String content;
    private int memberNo;
    private Integer parentNo;
    private String isAnswered;
    private int viewCount;
    private LocalDateTime createdAt;

    public Qna toEntity(){
        return Qna.builder()
                .no(no).type1(type1).type2(type2).title(title)
                .content(content).memberNo(memberNo).parentNo(parentNo)
                .isAnswered(isAnswered).viewCount(viewCount).createdAt(createdAt)
                .build();
    }
}