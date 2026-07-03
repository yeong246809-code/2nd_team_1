package org.example.k_market.dto;

import org.example.k_market.entity.Policy;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PolicyDTO {

    private long id;
    private String policyType;
    private String content;

    public Policy toEntity(){
        return Policy.builder()
                .id(id)
                .policyType(policyType)
                .content(content)
                .build();
    }
}