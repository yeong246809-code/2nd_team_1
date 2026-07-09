package org.example.k_market.dto;

import org.example.k_market.entity.Shop;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopDTO {

    private int memberNo;
    private Integer shopNo;
    private String name;
    private String ceo;
    private String bizNumber;
    private String mailOrderNumber;
    private String phone;
    private String fax;
    private String zipCode;
    private String baseAddress;
    private String detailAddress;
    private String status;
    private LocalDateTime rdate;

    public Shop toEntity(){
        return Shop.builder()
                .memberNo(memberNo).shopNo(shopNo).name(name).ceo(ceo)
                .bizNumber(bizNumber).mailOrderNumber(mailOrderNumber)
                .phone(phone).fax(fax).zipCode(zipCode).baseAddress(baseAddress)
                .detailAddress(detailAddress).status(status)
                .rdate(rdate)
                .build();
    }
}