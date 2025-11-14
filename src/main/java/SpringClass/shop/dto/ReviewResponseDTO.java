package SpringClass.shop.dto;

import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
    private Long id;
    private ProductSummaryDTO product;
    private UserSummaryDTO user;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> images;
    private int likeCount;
}
