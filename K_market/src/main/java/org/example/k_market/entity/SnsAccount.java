package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.SnsAccountDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "sns_account")
public class SnsAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int snsNo;
    private int memNo;
    private String provider;
    private String providerId;
    private LocalDateTime connectedAt;

    public SnsAccountDTO toDTO(){
        return SnsAccountDTO.builder()
                .snsNo(snsNo).memNo(memNo).provider(provider)
                .providerId(providerId).connectedAt(connectedAt)
                .build();
    }
}