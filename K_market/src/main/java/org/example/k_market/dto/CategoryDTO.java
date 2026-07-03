package kr.co.k_market.dto;

import kr.co.k_market.entity.Category;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTO {

    private int catNo;
    private String name;
    private Integer parentNo;
    private int depth;

    public Category toEntity(){
        return Category.builder()
                .catNo(catNo).name(name).parentNo(parentNo).depth(depth)
                .build();
    }
}