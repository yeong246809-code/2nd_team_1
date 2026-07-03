package kr.co.kmarket.entity;

import jakarta.persistence.*;
import kr.co.kmarket.dto.ProductOptionsDTO;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "product_options")
public class ProductOptions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long optionNo;
    private long prodNo;
    private String name;

    public ProductOptionsDTO toDTO(){
        return ProductOptionsDTO.builder()
                .optionNo(optionNo).prodNo(prodNo).name(name)
                .build();
    }
}