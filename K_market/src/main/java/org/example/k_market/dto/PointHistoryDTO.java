package kr.co.k_market.dto;

import kr.co.k_market.entity.PointHistory;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointHistoryDTO {

    private long pointNo;
    private int memberNo;
    private int amount;
    private int remainedAmount;
    private String description;
    private LocalDateTime createdAt;
    private LocalDate expiredAt;

    public PointHistory toEntity(){
        return PointHistory.builder()
                .pointNo(pointNo).memberNo(memberNo).amount(amount)
                .remainedAmount(remainedAmount).description(description)
                .createdAt(createdAt).expiredAt(expiredAt)
                .build();
    }
}