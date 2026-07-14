package org.example.k_market.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// columnList도 실제 DB 컬럼명인 visit_date로 맞춰주는 것이 안전합니다!
@Table(name = "visitor_log",
        indexes = @Index(name = "idx_visit_date", columnList = "visit_date"))
public class VisitorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // <-- [핵심 수정!] DB의 실제 컬럼명(visit_date)을 정확히 매핑해 줍니다.
    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    // <-- [핵심 수정!] DB의 실제 컬럼명(session_id)을 정확히 매핑해 줍니다.
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    // <-- [핵심 수정!] DB의 실제 컬럼명(ip_address)을 정확히 매핑해 줍니다.
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}