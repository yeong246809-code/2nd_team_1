package kr.co.kmarket.entity;

import jakarta.persistence.*;
import kr.co.kmarket.dto.ReviewDTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long reviewNO; 
    private long productNo;
    private int memberNo;
    private int rating;
    private String content;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private LocalDateTime createdAt;

    public ReviewDTO toDTO(){
        return ReviewDTO.builder()
                .reviewNO(reviewNO).productNo(productNo).memberNo(memberNo)
                .rating(rating).content(content).imageUrl1(imageUrl1)
                .imageUrl2(imageUrl2).imageUrl3(imageUrl3).createdAt(createdAt)
                .build();
    }
}