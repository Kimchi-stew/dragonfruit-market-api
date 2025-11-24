package SpringClass.shop.dto.Reviews;

import SpringClass.shop.dto.Users.UserSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReviewListDTO {
    private Long id;
    private UserSummaryDTO user;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private String image; // 첫번째 이미지만
    private int likeCount;
}
