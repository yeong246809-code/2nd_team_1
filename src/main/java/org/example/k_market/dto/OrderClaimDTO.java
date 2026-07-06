package org.example.k_market.dto;

import org.example.k_market.entity.OrderClaim;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderClaimDTO {

    private long id;
    private long orderDetailNo;
    private int memberNo;
    private String type;
    private String reasonType;
    private String reasonDetail;
    private String attachedImage;
    private String status;
    private LocalDateTime createdAt;

    public OrderClaim toEntity(){
        return OrderClaim.builder()
                .id(id).orderDetailNo(orderDetailNo).memberNo(memberNo)
                .type(type).reasonType(reasonType).reasonDetail(reasonDetail)
                .attachedImage(attachedImage).status(status).createdAt(createdAt)
                .build();
    }
}