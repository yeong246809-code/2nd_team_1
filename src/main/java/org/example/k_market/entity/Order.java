package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.OrderDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "`order`")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderNo;

    // memberNo는 User 엔티티와의 연관관계를 통해 관리되므로 별도 필드로 두기보다는
    // getter/setter를 통해 user 엔티티에서 접근하는 것을 권장합니다.
    private int memberNo;

    private String orderName;
    private String paymentMethod;
    private int totalProductPrice;
    private int totalDiscountPrice;
    private int totalShippingFee;
    private int usedPoints;
    private int totalPaymentPrice;
    private LocalDateTime createdAt;

    // 상태 변경을 위한 메서드 (명칭 수정)
    @Setter
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberNo", insertable = false, updatable = false)
    private Users user;

    private String recipientName;
    private String recipientPhone;
    private String zipCode;        // 추가
    private String baseAddress;
    private String detailAddress;  // 추가
    private String memo;           // 추가
    private String ordererName;
    private String ordererPhone;
    private String ordererZipCode;
    private String ordererBaseAddress;
    private String ordererDetailAddress;

    // --- 수정 및 추가 메서드 ---

    // 서비스에서 배송 정보를 엔티티에 일시적으로 담아 DTO로 넘길 때 사용
    public void setDeliveryInfo(String recipientName, String recipientPhone, String zipCode,
                                String baseAddress, String detailAddress, String memo) {
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.zipCode = zipCode;
        this.baseAddress = baseAddress;
        this.detailAddress = detailAddress;
        this.memo = memo;
    }

    public OrderDTO toDTO() {
        return OrderDTO.builder()
                .orderNo(this.orderNo)
                .orderName(this.orderName)
                .memberNo(this.memberNo)
                .id(this.user != null ? this.user.getId() : "비회원")
                .ordererName(this.ordererName)
                .ordererPhone(this.ordererPhone)
                .ordererZipCode(this.ordererZipCode)
                .ordererBaseAddress(this.ordererBaseAddress)
                .ordererDetailAddress(this.ordererDetailAddress)
                .recipientName(this.recipientName)
                .recipientPhone(this.recipientPhone)
                .zipCode(this.zipCode)
                .baseAddress(this.baseAddress)
                .detailAddress(this.detailAddress)
                .memo(this.memo)
                .paymentMethod(this.paymentMethod)
                .totalProductPrice(this.totalProductPrice)
                .totalDiscountPrice(this.totalDiscountPrice)
                .totalShippingFee(this.totalShippingFee)
                .usedPoints(this.usedPoints)
                .totalPaymentPrice(this.totalPaymentPrice)
                .createdAt(this.createdAt)
                .status(this.status)
                .build();
    }
}
