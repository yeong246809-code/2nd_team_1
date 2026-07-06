package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.OrderClaimDTO;
import lombok.*;
import org.example.k_market.dto.UsersDTO;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int memNo;
    private String id;
    private String pass;
    private String role;
    private String status;
    private String createdAt;

    public UsersDTO toDto() {
        return UsersDTO.builder().memNo(memNo).id(id).pass(pass).role(role).status(status).createdAt(createdAt).build();
    }
}