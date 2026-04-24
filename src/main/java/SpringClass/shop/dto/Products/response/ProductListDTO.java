package SpringClass.shop.dto.Products.response;

import SpringClass.shop.dto.Sellers.response.SellerSummaryDTO;
import SpringClass.shop.entity.Products.ProductImages;
import SpringClass.shop.entity.Products.Products;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

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
    
    public static ProductListDTO from(Products products) {
        return ProductListDTO.builder()
                .id(products.getId())
                .seller(SellerSummaryDTO.from(products.getSeller()))
                .name(products.getName())
                .price(products.getPrice())
                .image(products.getImages().stream()
                        .findFirst().map(ProductImages::getImageUrl)
                        .orElse(null))
                .likeCount(products.getLikeCount())
                .build();
    }
}

   
