package org.example.k_market.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "memberNo",
            referencedColumnName = "memberNo",
            insertable = false,
            updatable = false
    )
    private Users user;

}