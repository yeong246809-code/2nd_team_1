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
@Builder(toBuilder = true)
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int memberNo;
    private String id;
    private String pass;
    private String role;
    private String status;
    private String createdAt;

    public UsersDTO toDto() {
        return UsersDTO.builder().memberNo(memberNo).id(id).pass(pass).role(role).status(status).createdAt(createdAt).build();
    }
}