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
    private String orderName;

    private String paymentMethod;
    private int totalProductPrice;
    private int totalDiscountPrice;
    private int totalShippingFee;
    private int usedPoints;
    private int totalPaymentPrice;
    private LocalDateTime createdAt;

    private String status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberNo", insertable = false, updatable = false) // order 테이블의 memberNo 컬럼과 조인
    private Users user; // Member 대신 User 엔티티 사용

    public OrderDTO toDTO() {
        return OrderDTO.builder()
                .orderNo(this.orderNo)
                .orderName(this.orderName)
                .memberNo(this.memberNo)
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