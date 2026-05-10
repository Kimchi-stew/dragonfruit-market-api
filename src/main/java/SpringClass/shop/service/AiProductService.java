package SpringClass.shop.service;

import SpringClass.shop.dto.Products.response.AiProductContextResponse;
import SpringClass.shop.entity.Products.ProductCategories;
import SpringClass.shop.entity.Products.ProductImages;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.exceptions.product.ProductNotFoundException;
import SpringClass.shop.repository.Products.ProductCategoriesRepository;
import SpringClass.shop.repository.Products.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiProductService {

    private final ProductsRepository productsRepository;
    private final ProductCategoriesRepository productCategoriesRepository;

    public AiProductContextResponse getProductContext(Long productId) {
        Products product = productsRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));

        List<String> categories = productCategoriesRepository.findAllByProduct(product).stream()
                .map(pc -> pc.getCategory().getName())
                .collect(Collectors.toList());

        List<String> images = product.getImages() != null
                ? product.getImages().stream()
                        .map(ProductImages::getImageUrl)
                        .collect(Collectors.toList())
                : List.of();

        return AiProductContextResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .sellerName(product.getSeller().getStoreName())
                .categories(categories)
                .images(images)
                .build();
    }
}
