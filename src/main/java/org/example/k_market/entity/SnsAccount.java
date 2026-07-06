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
    private int memberNo;
    private String provider;
    private String providerId;
    private LocalDateTime connectedAt;

    public SnsAccountDTO toDTO(){
        return SnsAccountDTO.builder()
                .memberNo(memberNo).provider(provider)
                .providerId(providerId).connectedAt(connectedAt)
                .build();
    }
}