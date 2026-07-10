package org.example.k_market.dto;

import org.example.k_market.entity.Qna;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QnaDTO {

    // Users 테이블의 id를 화면에 출력하기 위한 필드
    // qna 테이블에 저장되는 값이 아니라 JOIN 조회 결과를 담는 용도
    private String id;
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

    // 신규 추가: 상품 상세페이지에서 넘어온 문의일 경우 상품번호 (일반 1:1문의는 null)
    private Long prodNo;

    public Qna toEntity(){
        return Qna.builder()
                .no(no).type1(type1).type2(type2).title(title)
                .content(content).memberNo(memberNo).parentNo(parentNo)
                .isAnswered(isAnswered).viewCount(viewCount).createdAt(createdAt)
                .prodNo(prodNo)
                .build();
    }
}