package org.example.k_market.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.k_market.entity.Order;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {

    private int orderNo;
    private String id;
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

    private String recipientName;    // 수령인
    private String recipientPhone;   // 연락처
    private String zipCode;          // 우편번호 (추가됨)
    private String baseAddress;      // 기본주소
    private String detailAddress;    // 상세주소 (추가됨)
    private String memo;             // 기타(메모) (추가됨)

    // 회원 이름 (엔티티 외에 조인 등으로 가져오는 경우)
    private String memberName;

    // 상품 정보는 상세 리스트로 충분합니다.
    private List<OrderDetailsDTO> orderItems;

    public Order toEntity(){
        return Order.builder()
                .orderNo(orderNo)
                .memberNo(memberNo)
                .orderName(orderName)
                .paymentMethod(paymentMethod)
                .totalProductPrice(totalProductPrice)
                .totalDiscountPrice(totalDiscountPrice)
                .totalShippingFee(totalShippingFee)
                .usedPoints(usedPoints)
                .totalPaymentPrice(totalPaymentPrice)
                .createdAt(createdAt)
                .status(status)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .zipCode(zipCode)
                .baseAddress(baseAddress)
                .detailAddress(detailAddress)
                .memo(memo)
                .build();
    }
}
