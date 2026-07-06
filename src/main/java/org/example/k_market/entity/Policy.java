package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.PolicyDTO;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "policy")
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String policyType;
    private String content;

    public PolicyDTO toDTO(){
        return PolicyDTO.builder()
                .id(id)
                .policyType(policyType)
                .content(content)
                .build();
    }
}