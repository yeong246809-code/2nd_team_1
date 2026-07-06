package org.example.k_market.dto;

import org.example.k_market.entity.Version;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VersionDTO {

    private long id;
    private String versionCode;
    private int memberNo;
    private LocalDateTime createdAt;
    private String changeLog;

    //추가필드 - 외래키
    private String memberId;

    public Version toEntity(){
        return Version.builder()
                .id(id).versionCode(versionCode).memberNo(memberNo)
                .createdAt(createdAt).changeLog(changeLog)
                .build();
    }
}