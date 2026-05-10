package SpringClass.shop.dto.Products.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AiProductContextResponse {
    private Long productId;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String sellerName;
    private List<String> categories;
    private List<String> images;
}
