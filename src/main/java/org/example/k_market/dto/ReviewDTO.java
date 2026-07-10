package org.example.k_market.dto;

import org.example.k_market.entity.Review;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewDTO {

    private long reviewNO;
    private long prodNo;
    private int memberNo;
    private int rating;
    private String content;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private LocalDateTime createdAt;

    public Review toEntity(){
        return Review.builder()
                .reviewNO(reviewNO).prodNo(prodNo).memberNo(memberNo)
                .rating(rating).content(content).imageUrl1(imageUrl1)
                .imageUrl2(imageUrl2).imageUrl3(imageUrl3).createdAt(createdAt)
                .build();
    }
}