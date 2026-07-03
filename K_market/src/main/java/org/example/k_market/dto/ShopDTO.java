package kr.co.k_market.dto;

import kr.co.k_market.entity.Shop;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopDTO {

    private long shopNo;
    private String id;
    private String pass;
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
    private String manageStatus;
    private LocalDateTime rdate;

    public Shop toEntity(){
        return Shop.builder()
                .shopNo(shopNo).id(id).pass(pass).name(name).ceo(ceo)
                .bizNumber(bizNumber).mailOrderNumber(mailOrderNumber)
                .phone(phone).fax(fax).zipCode(zipCode).baseAddress(baseAddress)
                .detailAddress(detailAddress).status(status).manageStatus(manageStatus)
                .rdate(rdate)
                .build();
    }
}