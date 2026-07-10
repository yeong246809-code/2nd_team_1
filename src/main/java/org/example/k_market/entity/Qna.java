package org.example.k_market.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;

@Entity
@Table(name = "qna")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Qna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer no;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    private String isAnswered;

    private Integer memberNo;

    private Integer parentNo;

    private String title;

    private String type1;

    private String type2;

    private Integer viewCount;


    // 신규 추가: 이 문의가 특정 상품에 대한 문의일 경우 상품번호 저장 (일반 1:1 문의는 null)
    private Long prodNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "memberNo",
            referencedColumnName = "memberNo",
            insertable = false,
            updatable = false
    )
    private Users user;

    @Transient
    public String getMaskedId() {

        if (user == null || user.getId() == null) {
            return "";
        }

        String id = user.getId();

        if (id.length() <= 2) {
            return id.substring(0, 1) + "*";
        }

        int visible = (id.length() + 1) / 2;   // 절반(홀수는 하나 더 보이게)

        return id.substring(0, visible)
                + "*".repeat(id.length() - visible);
    }




}