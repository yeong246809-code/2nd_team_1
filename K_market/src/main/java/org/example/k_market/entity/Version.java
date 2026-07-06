package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.VersionDTO;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "version")
public class Version {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String versionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberNo")
    private Users users;
    @Column(name = "memberNo", insertable = false, updatable = false)
    private int memberNo;

    @CreationTimestamp // ★ 이 어노테이션이 핵심입니다!
    @Column(name = "createdAt", nullable = true, updatable = false)
    private LocalDateTime createdAt;
    private String changeLog;

    public VersionDTO toDTO() {
        return VersionDTO.builder()
                .id(id)
                .versionCode(versionCode)
                .memberNo(this.users != null ? this.users.getMemberNo() : 0) // Users 객체에서 가져옴
                .memberId(this.users != null ? this.users.getId() : "알수없음") // 아이디 세팅
                .createdAt(createdAt)
                .changeLog(changeLog)
                .build();
    }
}