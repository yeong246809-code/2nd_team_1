package kr.co.kmarket.entity;

import jakarta.persistence.*;
import kr.co.kmarket.dto.ProductOptionItemsDTO;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "product_option_items")
public class ProductOptionItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long optItemNo;
    private long optionNo;
    private String itemName;
    private int addPrice;
    private int stock;

    public ProductOptionItemsDTO toDTO(){
        return ProductOptionItemsDTO.builder()
                .optItemNo(optItemNo).optionNo(optionNo)
                .itemName(itemName).addPrice(addPrice).stock(stock)
                .build();
    }
}