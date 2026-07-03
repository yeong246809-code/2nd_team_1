package kr.co.kmarket.entity;

import jakarta.persistence.*;
import kr.co.kmarket.dto.OrderClaimDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "order_claim")
public class OrderClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long orderDetailNo;
    private int memberNo;
    private String type;
    private String reasonType;
    private String reasonDetail;
    private String attachedImage;
    private String status;
    private LocalDateTime createdAt;

    public OrderClaimDTO toDTO(){
        return OrderClaimDTO.builder()
                .id(id).orderDetailNo(orderDetailNo).memberNo(memberNo)
                .type(type).reasonType(reasonType).reasonDetail(reasonDetail)
                .attachedImage(attachedImage).status(status).createdAt(createdAt)
                .build();
    }
}