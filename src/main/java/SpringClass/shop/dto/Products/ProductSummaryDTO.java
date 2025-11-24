package SpringClass.shop.dto.Products;

import SpringClass.shop.dto.Sellers.SellerSummaryDTO;
import SpringClass.shop.entity.Products.Products;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProductSummaryDTO {
    private Long id;
    private SellerSummaryDTO seller;
    private String name;
    private BigDecimal price;

    public static ProductSummaryDTO from(Products product) {
        return ProductSummaryDTO.builder()
                .id(product.getId())
                .seller(SellerSummaryDTO.from(product.getSeller()))
                .name(product.getName())
                .price(product.getPrice())
                .build();
    }
}
