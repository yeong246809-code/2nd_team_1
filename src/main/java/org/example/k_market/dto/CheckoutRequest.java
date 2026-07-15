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
    private Long couponDetailNo;
    private String ordererName;
    private String ordererPhone;
    private String ordererZipCode;
    private String ordererBaseAddress;
    private String ordererDetailAddress;
    private List<Shipment> shipments;

    public boolean isCartOrder() {
        return cartNos != null && !cartNos.isEmpty();
    }

    @Getter
    @Setter
    public static class Shipment {
        private String recipientName;
        private String recipientPhone;
        private String zipCode;
        private String baseAddress;
        private String detailAddress;
        private String memo;
        private List<ShipmentItem> items;
    }

    @Getter
    @Setter
    public static class ShipmentItem {
        private String itemKey;
        private Integer quantity;
    }
}
