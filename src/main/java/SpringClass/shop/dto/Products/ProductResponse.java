package SpringClass.shop.dto.Products;

import SpringClass.shop.dto.Sellers.SellerSummaryDTO;
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
    private String category;
    private Double rating; // 평균 평점
}
