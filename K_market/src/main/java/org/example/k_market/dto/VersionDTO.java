package kr.co.k_market.dto;

import kr.co.k_market.entity.Version;
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

    public Version toEntity(){
        return Version.builder()
                .id(id).versionCode(versionCode).memberNo(memberNo)
                .createdAt(createdAt).changeLog(changeLog)
                .build();
    }
}