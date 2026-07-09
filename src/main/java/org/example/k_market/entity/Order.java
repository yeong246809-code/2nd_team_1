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
    private int orderNo; // 테이블의 int(11) 구조에 맞춰 long -> int로 변경

    @Column(name = "memberNo", insertable = false, updatable = false)
    private int memberNo;

    private String paymentMethod;
    private int totalProductPrice;
    private int totalDiscountPrice;
    private int totalShippingFee;
    private int usedPoints;
    private int totalPaymentPrice;
    private LocalDateTime createdAt;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberNo", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberNo", insertable = false, updatable = false) // order 테이블의 memberNo 컬럼과 조인
    private Users user; // Member 대신 User 엔티티 사용

    public OrderDTO toDTO() {
        return OrderDTO.builder()
                .orderNo(this.orderNo)
                // .id(this.member != null ? this.member.getId() : "탈퇴회원")
                // .memberName(this.member != null ? this.member.getName() : "탈퇴회원")
                .recipientName(toDTO().getRecipientName())
                .recipientPhone(toDTO().getRecipientPhone())
                .baseAddress(toDTO().getBaseAddress())
                .prodNo(toDTO().getProdNo())
                .name(toDTO().getName())
                .price(toDTO().getPrice())
                .discountRate(toDTO().getDiscountRate())
                .stockQuantity(toDTO().getStockQuantity())
                .memberNo(this.memberNo)
                .paymentMethod(this.paymentMethod)
                .totalProductPrice(this.totalProductPrice)
                .totalDiscountPrice(this.totalDiscountPrice)
                .totalShippingFee(this.totalShippingFee)
                .usedPoints(this.usedPoints)
                .totalPaymentPrice(this.totalPaymentPrice)
                .createdAt(this.createdAt)
                .status(this.status) // 새로 추가한 상태값 반영
                .build();
    }
}