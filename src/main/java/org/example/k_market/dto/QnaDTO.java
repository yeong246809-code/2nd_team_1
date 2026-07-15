package org.example.k_market.dto;

import lombok.*;
import org.example.k_market.entity.Qna;

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

    // 신규 등록 시 null이어야 하므로 int가 아니라 Integer 사용
    private Integer no;

    private String type1;
    private String type2;
    private String title;
    private String content;

    private Integer memberNo;

    // 원글은 0, 답변은 원글 번호
    private Integer parentNo;

    private String isAnswered;

    private Integer viewCount;
    private LocalDateTime createdAt;

    // 상품 문의일 경우 상품 번호, 일반 문의는 null
    private Long prodNo;

    public Qna toEntity() {
        return Qna.builder()
                // 신규 등록에서는 no를 넣지 않는다.
                .type1(type1)
                .type2(type2)
                .title(title)
                .content(content)
                .memberNo(memberNo)
                .parentNo(parentNo == null ? 0 : parentNo)
                .isAnswered(
                        isAnswered == null || isAnswered.isBlank()
                                ? "답변대기"
                                : isAnswered
                )
                .viewCount(viewCount == null ? 0 : viewCount)
                .createdAt(
                        createdAt == null
                                ? LocalDateTime.now()
                                : createdAt
                )
                .prodNo(prodNo)
                .build();
    }
}