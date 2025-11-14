package SpringClass.shop.service;

import SpringClass.shop.dto.*;
import SpringClass.shop.entity.Products.ProductLikes;
import SpringClass.shop.entity.Products.ProductWish;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.SellerFollow;
import SpringClass.shop.entity.Sellers.SellerLikes;
import SpringClass.shop.entity.Users;
import SpringClass.shop.exceptions.PasswordMismatchException;
import SpringClass.shop.repository.*;
import SpringClass.shop.security.AuthenticatedUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AuthenticatedUserUtils authenticatedUserUtils;
    private final ProductLikeRepository productLikeRepository;
    private final ProductsRepository productsRepository;
    private final ProductWishRepository productWishRepository;
    private final SellerLikesRepository sellerLikesRepository;
    private final SellerFollowRepository sellerFollowRepository;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getProfile() {
        Users user = authenticatedUserUtils.getCurrentUser();
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .gender(user.getGender())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public String patchPassword(UserPasswordDTO request) {
        Users user = authenticatedUserUtils.getCurrentUser();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);
        return "비밀번호가 변경되었습니다.";
    }

    public UserProfileResponse patchProfile(UserProfileRequest request) {
        Users user = authenticatedUserUtils.getCurrentUser();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setGender(request.getGender());
        user.setProfileImage(request.getProfileImage());
        usersRepository.save(user);
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .gender(user.getGender())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

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

    public List<SellerListDTO> getLikeSellers() {
        Users user = authenticatedUserUtils.getCurrentUser();
        List<SellerLikes> sellerLikes = sellerLikesRepository.findByUserAndSellers_DeletedAtIsNull(user);

        return sellerLikes.stream()
                .map(seller -> SellerListDTO.builder()
                        .id(seller.getSellers().getId())
                        .storeName(seller.getSellers().getStoreName())
                        .image(seller.getSellers().getImage())
                        .likeCount(seller.getSellers().getLikeCount())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SellerListDTO> getFollowSellers() {
        Users user = authenticatedUserUtils.getCurrentUser();
        List<SellerFollow> sellerFollows = sellerFollowRepository.findByUserAndSellers_DeletedAtIsNull(user);
        return sellerFollows.stream()
                .map(sellerFollow -> SellerListDTO.builder()
                        .id(sellerFollow.getSellers().getId())
                        .storeName(sellerFollow.getSellers().getStoreName())
                        .image(sellerFollow.getSellers().getImage())
                        .likeCount(sellerFollow.getSellers().getLikeCount())
                        .build())
                .collect(Collectors.toList());
    }
}
