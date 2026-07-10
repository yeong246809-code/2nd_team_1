package org.example.k_market.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminPointListDTO {
    private long pointNo;
    private String id;              // Users 테이블의 아이디
    private String name;            // Member 테이블의 이름
    private int amount;             // 포인트 증감량
    private int remainedAmount;     // 잔여 포인트
    private String description;     // 지급/사용 내용
    private LocalDateTime createdAt;// 발생 일시
}