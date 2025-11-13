package SpringClass.shop.service;

import SpringClass.shop.dto.ProductListDTO;
import SpringClass.shop.dto.SellerSummaryDTO;
import SpringClass.shop.entity.Products.ProductLikes;
import SpringClass.shop.entity.Products.ProductWish;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Users;
import SpringClass.shop.exceptions.ProductNotFoundException;
import SpringClass.shop.repository.*;
import SpringClass.shop.security.AuthenticatedUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AuthenticatedUserUtils authenticatedUserUtils;
    private final ProductLikeRepository productLikeRepository;
    private final ProductsRepository productsRepository;
    private final ProductWishRepository productWishRepository;

    public List<ProductListDTO> getLikeProducts() {
        // user 정보 가져오기 (baarer token에서 추출)
        Users user = authenticatedUserUtils.getCurrentUser();
        List<ProductLikes> likedProducts = productLikeRepository.findByUserAndProduct_DeletedAtIsNull(user);

        return likedProducts.stream()
                .map(like -> {
                    Products product = like.getProduct();
                    // 대표 이미지 (첫 번째 이미지)
                    String mainImage = null;
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        mainImage = product.getImages().get(0).getImageUrl();
                    }

                    return ProductListDTO.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .price(product.getPrice())
                            .likeCount(product.getLikeCount())
                            .image(mainImage)
                            .seller(SellerSummaryDTO.builder()
                                    .id(product.getSeller().getId())
                                    .storeName(product.getSeller().getStoreName())
                                    .image(product.getSeller().getImage())
                                    .build())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<ProductListDTO> getWishProducts() {
        Users user = authenticatedUserUtils.getCurrentUser();
        List<ProductWish> wishProducts = productWishRepository.findByUserAndProduct_DeletedAtIsNull(user);

        return wishProducts.stream()
                .map(like -> {
                    Products product = like.getProduct();
                    // 대표 이미지 (첫 번째 이미지)
                    String mainImage = null;
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        mainImage = product.getImages().get(0).getImageUrl();
                    }

                    return ProductListDTO.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .price(product.getPrice())
                            .likeCount(product.getLikeCount())
                            .image(mainImage)
                            .seller(SellerSummaryDTO.builder()
                                    .id(product.getSeller().getId())
                                    .storeName(product.getSeller().getStoreName())
                                    .image(product.getSeller().getImage())
                                    .build())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
