package SpringClass.shop.service;

import SpringClass.shop.dto.Products.response.ProductListDTO;
import SpringClass.shop.dto.Reviews.response.ReviewListDTO;
import SpringClass.shop.dto.Sellers.response.SellerListDTO;
import SpringClass.shop.dto.Sellers.response.SellerResponse;
import SpringClass.shop.dto.Sellers.response.SellerSummaryDTO;
import SpringClass.shop.dto.Users.request.SignupRequest;
import SpringClass.shop.dto.Users.request.UserPasswordDTO;
import SpringClass.shop.dto.Users.request.UserProfileRequest;
import SpringClass.shop.dto.Users.response.UserProfileResponse;
import SpringClass.shop.dto.Users.response.UserSummaryDTO;
import SpringClass.shop.entity.Products.ProductLikes;
import SpringClass.shop.entity.Products.ProductWish;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Reviews.Reviews;
import SpringClass.shop.entity.Sellers.SellerFollow;
import SpringClass.shop.entity.Sellers.SellerLikes;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.PasswordMismatchException;
import SpringClass.shop.exceptions.SellerNotFoundException;
import SpringClass.shop.exceptions.UserAlreadyExistException;
import SpringClass.shop.repository.Products.ProductLikeRepository;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.repository.Products.ProductWishRepository;
import SpringClass.shop.repository.Reviews.ReviewRepository;
import SpringClass.shop.repository.Sellers.SellerFollowRepository;
import SpringClass.shop.repository.Sellers.SellerLikesRepository;
import SpringClass.shop.repository.Sellers.SellersRepository;
import SpringClass.shop.repository.Users.UsersRepository;
import SpringClass.shop.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final SecurityUtils SecurityUtils;
    private final ProductLikeRepository productLikeRepository;
    private final ProductsRepository productsRepository;
    private final ProductWishRepository productWishRepository;
    private final SellerLikesRepository sellerLikesRepository;
    private final SellerFollowRepository sellerFollowRepository;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReviewRepository reviewRepository;
    private final SellersRepository sellersRepository;


    public void signup(SignupRequest request) {
        if (usersRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistException("이미 존재하는 계정입니다.");
        }

        Users user = Users.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .gender(request.getGender())
                .userRole(UserRole.USER)
                .profileImage(null)
                .build();
        usersRepository.save(user);
    }

    public UserProfileResponse getProfile() {
        Users user = SecurityUtils.getCurrentUser();
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
        Users user = SecurityUtils.getCurrentUser();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);
        return "비밀번호가 변경되었습니다.";
    }

    public UserProfileResponse patchProfile(UserProfileRequest request) {
        Users user = SecurityUtils.getCurrentUser();
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

    public List<ProductListDTO> getLikeProducts(String sort) {
        // user 정보 가져오기 (baarer token에서 추출)
        Users user = SecurityUtils.getCurrentUser();
        List<ProductLikes> likedProducts;
        if ("like".equalsIgnoreCase(sort)) {
            likedProducts = productLikeRepository.findByUserAndProduct_DeletedAtIsNullOrderByProduct_LikeCountDescCreatedAtDesc(user);
        } else if ("old".equalsIgnoreCase(sort)) {
            likedProducts = productLikeRepository.findByUserAndProduct_DeletedAtIsNullOrderByCreatedAtAsc(user);
        } else {
            likedProducts = productLikeRepository.findByUserAndProduct_DeletedAtIsNullOrderByCreatedAtDesc(user);
        }

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
        Users user = SecurityUtils.getCurrentUser();
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
        Users user = SecurityUtils.getCurrentUser();
        List<SellerLikes> sellerLikes = sellerLikesRepository.findByUserAndSellers_DeletedAtIsNull(user);

        return sellerLikes.stream()
                .map(seller -> SellerListDTO.builder()
                        .id(seller.getSellers().getId())
                        .storeName(seller.getSellers().getStoreName())
                        .image(seller.getSellers().getImage())
                        .likeCount(seller.getSellers().getLikeCount())
                        .followCount(seller.getSellers().getFollowCount())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SellerListDTO> getFollowSellers() {
        Users user = SecurityUtils.getCurrentUser();
        List<SellerFollow> sellerFollows = sellerFollowRepository.findByUserAndSellers_DeletedAtIsNull(user);
        return sellerFollows.stream()
                .map(sellerFollow -> SellerListDTO.builder()
                        .id(sellerFollow.getSellers().getId())
                        .storeName(sellerFollow.getSellers().getStoreName())
                        .image(sellerFollow.getSellers().getImage())
                        .likeCount(sellerFollow.getSellers().getLikeCount())
                        .followCount(sellerFollow.getSellers().getFollowCount())
                        .build())
                .collect(Collectors.toList());
    }

    public List<ReviewListDTO> getMyReviews() {
        Users user = SecurityUtils.getCurrentUser();
        // 최신순
        List<Reviews> reviews = reviewRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user);
        return reviews.stream()
                .map(review -> ReviewListDTO.builder()

                        .id(review.getId())
                        .user(UserSummaryDTO.from(review.getUser()))
                        .rating(review.getRating())
                        .createdAt(review.getCreatedAt())
                        .image(review.getImages().get(0).getImageUrl()) // 첫번째 이미지
                        .likeCount(review.getLikeCount())
                        .build())
                .collect(Collectors.toList());
    }

    public List<ProductListDTO> getMyProducts() {
        Users user = SecurityUtils.getCurrentUser();
        Sellers seller = sellersRepository.findByUser(user)
                .orElseThrow(() -> new SellerNotFoundException("해당 상점을 찾을 수 없습니다."));
        // 최신순
        List<Products> products = productsRepository.findAllBySellerAndDeletedAtIsNullOrderByCreatedAtDesc(seller);

        return products.stream()
                .map(product -> ProductListDTO.builder()
                        .id(product.getId())
                        .seller(SellerSummaryDTO.from(product.getSeller()))
                        .name(product.getName())
                        .price(product.getPrice())
                        .image(product.getImages().get(0).getImageUrl())
                        .likeCount(product.getLikeCount())
                        .build())
                .collect(Collectors.toList());
    }

    public SellerResponse getMySeller() {
        Users user = SecurityUtils.getCurrentUser();
        Sellers seller = sellersRepository.findByUser(user)
                .orElseThrow(() -> new SellerNotFoundException("상점이 존재하지 않습니다."));
        boolean followed = sellerFollowRepository.existsByUserAndSellers(user, seller);
        return SellerResponse.builder()
                .id(seller.getId())
                .userId(seller.getId())
                .storeName(seller.getStoreName())
                .description(seller.getDescription())
                .image(seller.getImage())
                .createdAt(seller.getCreatedAt())
                .likeCount(seller.getLikeCount())
                .followCount(seller.getFollowCount())
                .followed(followed)
                .build();

    }
}
