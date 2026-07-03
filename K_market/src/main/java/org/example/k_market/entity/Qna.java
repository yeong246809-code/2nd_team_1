package kr.co.kmarket.entity;

import jakarta.persistence.*;
import kr.co.kmarket.dto.QnaDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "qna")
public class Qna {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int no;
    private String type1;
    private String type2;
    private String title;
    private String content;
    private int memberNo;
    private Integer parentNo; // 부모 글 번호 (nullable)
    private String isAnswered;
    private int viewCount;
    private LocalDateTime createdAt;

    public QnaDTO toDTO(){
        return QnaDTO.builder()
                .no(no).type1(type1).type2(type2).title(title)
                .content(content).memberNo(memberNo).parentNo(parentNo)
                .isAnswered(isAnswered).viewCount(viewCount).createdAt(createdAt)
                .build();
    }
}