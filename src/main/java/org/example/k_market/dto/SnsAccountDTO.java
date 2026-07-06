package org.example.k_market.dto;

import org.example.k_market.entity.SnsAccount;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SnsAccountDTO {

    private int memberNo;
    private String provider;
    private String providerId;
    private LocalDateTime connectedAt;

    public SnsAccount toEntity(){
        return SnsAccount.builder()
                .memberNo(memberNo).provider(provider)
                .providerId(providerId).connectedAt(connectedAt)
                .build();
    }
}