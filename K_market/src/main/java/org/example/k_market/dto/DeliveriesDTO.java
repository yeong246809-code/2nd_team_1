package org.example.k_market.dto;

import org.example.k_market.entity.Deliveries;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveriesDTO {

    private long deliverNo;
    private long orderNo;
    private long orderDetailNo;
    private String trackingNumber;
    private String courierName;
    private String recipientName;
    private String recipientPhone;
    private String zipCode;
    private String baseAddress;
    private String detailAddress;
    private String deliveryStatus;
    private String memo;
    private LocalDateTime shippedAt;

    public Deliveries toEntity(){
        return Deliveries.builder()
                .deliverNo(deliverNo).orderNo(orderNo).orderDetailNo(orderDetailNo)
                .trackingNumber(trackingNumber).courierName(courierName)
                .recipientName(recipientName).recipientPhone(recipientPhone)
                .zipCode(zipCode).baseAddress(baseAddress).detailAddress(detailAddress)
                .deliveryStatus(deliveryStatus).memo(memo).shippedAt(shippedAt)
                .build();
    }
}