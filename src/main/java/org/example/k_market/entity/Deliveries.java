package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.DeliveryDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "deliveries")
public class Deliveries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long deliverNo;
    private long orderNo;
    private long orderDetailNo;
    private String trackingNumber;
    private String courierName;

    @Transient
    private String recipientName;

    @Transient
    private String recipientPhone;

    @Transient
    private String zipCode;

    @Transient
    private String baseAddress;

    @Transient
    private String detailAddress;

    @Transient
    private String deliveryStatus;

    @Transient
    private String memo;

    private LocalDateTime shippedAt;

    


    public DeliveryDTO toDTO(){
        return DeliveryDTO.builder()
                .deliverNo(deliverNo).orderNo(orderNo).orderDetailNo(orderDetailNo)
                .trackingNumber(trackingNumber).courierName(courierName)
                .recipientName(recipientName).recipientPhone(recipientPhone)
                .zipCode(zipCode).baseAddress(baseAddress).detailAddress(detailAddress)
                .deliveryStatus(deliveryStatus).memo(memo).shippedAt(shippedAt)
                .build();


    }
}