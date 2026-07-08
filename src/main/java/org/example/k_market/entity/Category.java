package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.CategoryDTO;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "cateNo")
    private int cateNo;

    private String name;
    private Integer parentNo;
    private int depth;

    public CategoryDTO toDTO(){
        return CategoryDTO.builder()
                .cateNo(cateNo).name(name).parentNo(parentNo).depth(depth)
                .build();
    }
}