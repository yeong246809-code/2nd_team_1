package org.example.k_market.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CheckoutRequest {
    private List<Long> cartNos;
    private Long directProdNo;
    private Long directSkuNo;
    private Integer directQuantity;
    private List<String> directSkuNos;
    private List<Integer> directQuantities;
    private String recipientName;
    private String recipientPhone;
    private String zipCode;
    private String baseAddress;
    private String detailAddress;
    private String memo;
    private String paymentMethod;
    private Integer usedPoints;

    public boolean isCartOrder() {
        return cartNos != null && !cartNos.isEmpty();
    }
}
