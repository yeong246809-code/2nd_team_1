package org.example.k_market.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryNodeDTO {
    // 프론트엔드 JS에서 'id'라는 이름으로 보내므로 변수명을 id로 맞춥니다 (DB의 cateNo와 매핑)
    private Integer id;
    private String name;

    // 2차 카테고리들을 담을 리스트 (기본값으로 빈 리스트 할당)
    @Builder.Default
    private List<CategoryNodeDTO> children = new ArrayList<>();
}
