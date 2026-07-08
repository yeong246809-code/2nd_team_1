package org.example.k_market.entity;

import jakarta.persistence.*;
import org.example.k_market.dto.BannerDTO;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "banner")
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String sizeType;
    private String backgroundColor;
    private String linkUrl;
    private String position;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String fileUrl;
    private String status;
    private Integer couponNo;

    public BannerDTO toDTO(){
        return BannerDTO.builder()
                .id(id).name(name).sizeType(sizeType).backgroundColor(backgroundColor)
                .linkUrl(linkUrl).position(position).startDate(startDate).endDate(endDate)
                .startTime(startTime).endTime(endTime).fileUrl(fileUrl)
                .status(status).couponNo(couponNo)
                .build();
    }

    public void updateStatus(String status) { this.status = status; }
}