package org.example.k_market.dto;

import jakarta.persistence.Transient;
import lombok.*;
import org.example.k_market.entity.Deliveries;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder // 빌더 패턴 자동 생성
public class DeliveryDTO {

    private long deliverNo;
    private long orderNo;
    private long orderDetailNo;
    private String trackingNumber;
    private String courierName;
    private String deliveryStatus;
    private LocalDateTime shippedAt;

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
    private String memo;


    // 엔티티 변환 메서드 (빌더 사용)
    public Deliveries toEntity() {
        return Deliveries.builder()
                .deliverNo(this.deliverNo)
                .orderNo(this.orderNo)
                .orderDetailNo(this.orderDetailNo)
                .trackingNumber(this.trackingNumber)
                .courierName(this.courierName)
                .deliveryStatus(this.deliveryStatus)
                .shippedAt(this.shippedAt)
                .build();
    }
}