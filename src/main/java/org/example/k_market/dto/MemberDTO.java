package org.example.k_market.dto;

import org.example.k_market.entity.Member;
import lombok.*;
import org.example.k_market.service.member.MemberAccountStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberDTO {

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
    private String status;
    private LocalDateTime lastLoginAt;
    private String memo;
    private String locationPolicyAgreed;

    private String id;

    public Member toEntity(){
        return Member.builder()
                .memberNo(memberNo).name(name).birthDate(birthDate)
                .gender(gender).email(email).phone(phone).zipCode(zipCode)
                .baseAddress(baseAddress).detailAddress(detailAddress)
                .gradeNo(gradeNo).points(points).createdAt(createdAt)
                .status(MemberAccountStatus.from(status))
                .lastLoginAt(lastLoginAt).memo(memo)
                .locationPolicyAgreed(locationPolicyAgreed).id(id)
                .build();
    }
}
