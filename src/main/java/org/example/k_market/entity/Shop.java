package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.ShopDTO;
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
    private int memberNo;
    @Column(insertable = false, updatable = false)
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

    public enum ShopStatus {
        ACTIVE, STOPPED, PENDING, DELETED
    }

    public ShopDTO toDTO(){
        return ShopDTO.builder()
                .memberNo(memberNo).shopNo(shopNo).name(name).ceo(ceo)
                .bizNumber(bizNumber).mailOrderNumber(mailOrderNumber)
                .phone(phone).fax(fax).zipCode(zipCode).baseAddress(baseAddress)
                .detailAddress(detailAddress).status(status)
                .rdate(rdate)
                .build();
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public void updateProfile(
            String name,
            String ceo,
            String mailOrderNumber,
            String phone,
            String fax,
            String zipCode,
            String baseAddress,
            String detailAddress) {
        this.name = name;
        this.ceo = ceo;
        this.mailOrderNumber = mailOrderNumber;
        this.phone = phone;
        this.fax = fax;
        this.zipCode = zipCode;
        this.baseAddress = baseAddress;
        this.detailAddress = detailAddress;
    }

    public void delete() {
        this.status = "DELETED";
    }

}
