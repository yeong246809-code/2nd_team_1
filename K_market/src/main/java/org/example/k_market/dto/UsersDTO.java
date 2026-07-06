package org.example.k_market.dto;

import lombok.*;
import org.example.k_market.entity.Users;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsersDTO {
    private int memNo;
    private String id;
    private String pass;
    private String role;
    private String status;
    private String createdAt;

    public Users toEntity() {
        return Users.builder().memNo(memNo).id(id).pass(pass).role(role).status(status).createdAt(createdAt).build();
    }
}
