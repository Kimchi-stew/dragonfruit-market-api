package SpringClass.shop.dto.Products;

import SpringClass.shop.dto.Sellers.SellerSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProductListDTO {
    private Long id;
    private SellerSummaryDTO seller;
    private String name;
    private BigDecimal price;
    private String image; // 대표 이미지 1개
    private int likeCount;
}
