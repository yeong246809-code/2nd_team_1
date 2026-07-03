package kr.co.k_market.dto;

import kr.co.k_market.entity.ProductOptionItems;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductOptionItemsDTO {

    private long optItemNo;
    private long optionNo;
    private String itemName;
    private int addPrice;
    private int stock;

    public ProductOptionItems toEntity(){
        return ProductOptionItems.builder()
                .optItemNo(optItemNo).optionNo(optionNo)
                .itemName(itemName).addPrice(addPrice).stock(stock)
                .build();
    }
}