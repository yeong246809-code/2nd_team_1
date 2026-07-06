package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.PointHistoryDTO;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "point_history")
public class PointHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long pointNo;
    private int memberNo;
    private int amount;
    private int remainedAmount;
    private String description;
    private LocalDateTime createdAt;
    private LocalDate expiredAt;

    public PointHistoryDTO toDTO(){
        return PointHistoryDTO.builder()
                .pointNo(pointNo).memberNo(memberNo).amount(amount)
                .remainedAmount(remainedAmount).description(description)
                .createdAt(createdAt).expiredAt(expiredAt)
                .build();
    }
}