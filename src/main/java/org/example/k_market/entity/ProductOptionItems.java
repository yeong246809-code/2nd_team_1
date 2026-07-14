package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.ProductOptionItemsDTO;
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
    @Column(name = "itemNo")
    private long optItemNo;

    private long optionNo;

    private String itemName;

    public ProductOptionItemsDTO toDTO(){
        return ProductOptionItemsDTO.builder()
                .optItemNo(optItemNo)
                .optionNo(optionNo)
                .itemName(itemName)
                .build();
    }
}