package kr.co.kmarket.entity;

import jakarta.persistence.*;
import kr.co.kmarket.dto.DeliveriesDTO;
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
    private String recipientName;
    private String recipientPhone;
    private String zipCode;
    private String baseAddress;
    private String detailAddress;
    private String deliveryStatus;
    private String memo;
    private LocalDateTime shippedAt;

    public DeliveriesDTO toDTO(){
        return DeliveriesDTO.builder()
                .deliverNo(deliverNo).orderNo(orderNo).orderDetailNo(orderDetailNo)
                .trackingNumber(trackingNumber).courierName(courierName)
                .recipientName(recipientName).recipientPhone(recipientPhone)
                .zipCode(zipCode).baseAddress(baseAddress).detailAddress(detailAddress)
                .deliveryStatus(deliveryStatus).memo(memo).shippedAt(shippedAt)
                .build();
    }
}