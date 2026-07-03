package kr.co.kmarket.entity;

import jakarta.persistence.*;
import kr.co.kmarket.dto.ShopDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "shop")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public ShopDTO toDTO(){
        return ShopDTO.builder()
                .shopNo(shopNo).id(id).pass(pass).name(name).ceo(ceo)
                .bizNumber(bizNumber).mailOrderNumber(mailOrderNumber)
                .phone(phone).fax(fax).zipCode(zipCode).baseAddress(baseAddress)
                .detailAddress(detailAddress).status(status).manageStatus(manageStatus)
                .rdate(rdate)
                .build();
    }
}