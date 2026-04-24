package SpringClass.shop.dto.Reviews.response;

import SpringClass.shop.dto.Products.response.ProductSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ReviewDeleteDTO {
    private ProductSummaryDTO products;
    private Integer rating;
    private String content;
    private int likeCount;
    private LocalDateTime deletedAt;

}
