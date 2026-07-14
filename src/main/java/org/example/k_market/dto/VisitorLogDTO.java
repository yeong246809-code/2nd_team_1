package org.example.k_market.dto;

import lombok.*;
import org.example.k_market.entity.VisitorLog;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorLogDTO {
    private Long id;
    private LocalDate visitDate;
    private String sessionId;
    private String ipAddress;

    public VisitorLog toEntity() {
        return VisitorLog.builder()
                .id(this.id)
                .visitDate(this.visitDate)
                .sessionId(this.sessionId)
                .ipAddress(this.ipAddress)
                .build();
    }
}