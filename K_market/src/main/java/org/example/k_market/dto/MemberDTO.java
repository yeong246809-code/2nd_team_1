package kr.co.k_market.dto;

import kr.co.k_market.entity.Member;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberDTO {

    private int memNo;
    private String id;
    private String pass;
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

    public Member toEntity(){
        return Member.builder()
                .memNo(memNo).id(id).pass(pass).name(name).birthDate(birthDate)
                .gender(gender).email(email).phone(phone).zipCode(zipCode)
                .baseAddress(baseAddress).detailAddress(detailAddress)
                .gradeNo(gradeNo).points(points).createdAt(createdAt)
                .status(status).lastLoginAt(lastLoginAt).memo(memo)
                .locationPolicyAgreed(locationPolicyAgreed)
                .build();
    }
}