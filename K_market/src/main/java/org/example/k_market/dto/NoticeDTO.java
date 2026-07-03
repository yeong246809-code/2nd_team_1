package kr.co.k_market.dto;

import kr.co.k_market.entity.Notice;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeDTO {

    private int no;
    private String type;
    private String title;
    private String content;
    private int viewCount;
    private int memberNo;
    private LocalDateTime createdAt;

    public Notice toEntity(){
        return Notice.builder()
                .no(no).type(type).title(title).content(content)
                .viewCount(viewCount).memberNo(memberNo).createdAt(createdAt)
                .build();
    }
}