package SpringClass.shop.dto.Products.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductRequest {
    private String name;
    private BigDecimal price;
    private String description;
    private Integer stock;
    private List<Long> mediaIds;
    private String category;
}
