package org.example.k_market.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.k_market.dto.NoticeDTO;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Setter
@Table(name = "notice")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int no;

    private String type;
    private String title;
    private String content;
    private int viewCount;
    private int memberNo;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;





}