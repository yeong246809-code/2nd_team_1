package org.example.k_market.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyInfoUpdateDTO {

    private String name;
    private String emailLocal;
    private String emailDomain;
    private String phoneFirst;
    private String phoneMiddle;
    private String phoneLast;
    private String zipCode;
    private String baseAddress;
    private String detailAddress;

    private String shopName;
    private String ceo;
    private String mailOrderNumber;
    private String shopPhone;
    private String fax;
    private String shopZipCode;
    private String shopBaseAddress;
    private String shopDetailAddress;
}
