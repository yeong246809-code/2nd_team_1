package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.MemberDTO;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int memNo;
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String email;
    private String phone;
    private String zipCode;
    private String baseAddress;
    private String detailAddress;
    private int gradeNo;
    private int points;
    private LocalDateTime createdAt;
    private String status;
    private LocalDateTime lastLoginAt;
    private String memo;
    private String locationPolicyAgreed;

    public MemberDTO toDTO(){
        return MemberDTO.builder()
                .memNo(memNo).name(name).birthDate(birthDate)
                .gender(gender).email(email).phone(phone).zipCode(zipCode)
                .baseAddress(baseAddress).detailAddress(detailAddress)
                .gradeNo(gradeNo).points(points).createdAt(createdAt)
                .status(status).lastLoginAt(lastLoginAt).memo(memo)
                .locationPolicyAgreed(locationPolicyAgreed)
                .build();
    }
}