package SpringClass.shop.dto.Sellers.response;

import SpringClass.shop.entity.Products.ProductCategories;
import SpringClass.shop.entity.Products.ProductImages;
import SpringClass.shop.entity.Products.Products;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SellerProductResponse {
    private Long productId;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private int likeCount;
    private String thumbnailUrl;
    private List<String> categories;

    public static SellerProductResponse from(Products product) {
        String thumbnail = product.getImages() == null || product.getImages().isEmpty()
                ? null
                : product.getImages().get(0).getImageUrl();

        List<String> categoryNames = product.getProductCategories() == null
                ? List.of()
                : product.getProductCategories().stream()
                        .map(pc -> pc.getCategory().getName())
                        .collect(Collectors.toList());

        return SellerProductResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .likeCount(product.getLikeCount())
                .thumbnailUrl(thumbnail)
                .categories(categoryNames)
                .build();
    }
}
