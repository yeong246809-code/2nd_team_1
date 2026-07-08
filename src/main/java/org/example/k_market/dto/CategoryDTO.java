package org.example.k_market.dto;

import org.example.k_market.entity.Category;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTO {

    private int cateNo;
    private String name;
    private Integer parentNo;
    private int depth;

    public Category toEntity(){
        return Category.builder()
                .cateNo(cateNo).name(name).parentNo(parentNo).depth(depth)
                .build();
    }
}