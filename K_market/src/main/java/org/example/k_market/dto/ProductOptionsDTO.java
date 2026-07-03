package org.example.k_market.dto;

import org.example.k_market.entity.ProductOptions;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductOptionsDTO {

    private long optionNo;
    private long prodNo;
    private String name;

    public ProductOptions toEntity(){
        return ProductOptions.builder()
                .optionNo(optionNo).prodNo(prodNo).name(name)
                .build();
    }
}