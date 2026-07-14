package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.MemberDTO;
import lombok.*;
import org.example.k_market.service.member.MemberAccountStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "member")
public class Member {
    @Id
    private int memberNo;
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
    @Enumerated(EnumType.STRING)
    private MemberAccountStatus status;
    private LocalDateTime lastLoginAt;
    private String memo;
    private String locationPolicyAgreed;

    public void changePoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("포인트 잔액은 0보다 작을 수 없습니다.");
        }
        this.points = points;
    }

    @Transient
    private String id;

    public MemberDTO toDTO(){
        return MemberDTO.builder()
                .memberNo(memberNo).name(name).birthDate(birthDate)
                .gender(gender).email(email).phone(phone).zipCode(zipCode)
                .baseAddress(baseAddress).detailAddress(detailAddress)
                .gradeNo(gradeNo).points(points).createdAt(createdAt)
                .status(status == null ? null : status.name())
                .lastLoginAt(lastLoginAt).memo(memo)
                .locationPolicyAgreed(locationPolicyAgreed).id(id)
                .build();
    }
}
