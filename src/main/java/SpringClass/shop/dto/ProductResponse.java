package SpringClass.shop.dto;

import SpringClass.shop.entity.Products.ProductImages;
import SpringClass.shop.entity.Products.ProductWish;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private SellerSummaryDTO seller;
    private String name;
    private BigDecimal price;
    private String description;
    private Integer stock;
    private int likeCount;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean wished;
}
