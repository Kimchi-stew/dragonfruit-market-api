package SpringClass.shop.dto.Products.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RecommendProductDTO {
    private Long productId;
    private String name;
    private BigDecimal price;
    private String thumbnailUrl;
    private Double score;
}
