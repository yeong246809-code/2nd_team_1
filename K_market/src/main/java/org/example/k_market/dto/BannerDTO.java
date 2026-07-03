package kr.co.k_market.dto;

import kr.co.k_market.entity.Banner;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BannerDTO {

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

    public Banner toEntity(){
        return Banner.builder()
                .id(id).name(name).sizeType(sizeType).backgroundColor(backgroundColor)
                .linkUrl(linkUrl).position(position).startDate(startDate).endDate(endDate)
                .startTime(startTime).endTime(endTime).fileUrl(fileUrl)
                .status(status).couponNo(couponNo)
                .build();
    }
}