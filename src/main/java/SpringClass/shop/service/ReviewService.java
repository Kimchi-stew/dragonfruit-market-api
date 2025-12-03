package SpringClass.shop.service;

import SpringClass.shop.dto.LikesResponseDTO;
import SpringClass.shop.dto.Products.ProductSummaryDTO;
import SpringClass.shop.dto.Reviews.ReviewDeleteDTO;
import SpringClass.shop.dto.Reviews.ReviewListDTO;
import SpringClass.shop.dto.Reviews.ReviewRequest;
import SpringClass.shop.dto.Reviews.ReviewResponseDTO;
import SpringClass.shop.dto.Users.UserSummaryDTO;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Reviews.ReviewImages;
import SpringClass.shop.entity.Reviews.ReviewLikes;
import SpringClass.shop.entity.Reviews.Reviews;
import SpringClass.shop.entity.Users;
import SpringClass.shop.enums.RatingSortType;
import SpringClass.shop.enums.SortType;
import SpringClass.shop.exceptions.ForbiddenException;
import SpringClass.shop.exceptions.ReviewNotFoundException;
import SpringClass.shop.repository.ProductsRepository;
import SpringClass.shop.repository.ReviewImagesRepository;
import SpringClass.shop.repository.ReviewLikesRepository;
import SpringClass.shop.repository.ReviewRepository;
import SpringClass.shop.security.AuthenticatedUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final AuthenticatedUserUtils authenticatedUserUtils;
    private final ReviewRepository reviewRepository;
    private final ProductsRepository productsRepository;
    private final ReviewImagesRepository reviewImagesRepository;
    private final ReviewLikesRepository reviewLikesRepository;
    private final NotificationService notificationService;

    public ReviewResponseDTO createReview(Long productId, ReviewRequest request) {
        Users user = authenticatedUserUtils.getCurrentUser();
        Products product = productsRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        Reviews reviews = Reviews.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .likeCount(0)
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        reviewRepository.save(reviews);
        // 이미지 변환
        List<String> imageUrls = request.getImages();
        List<ReviewImages> savedImages = new ArrayList<>();

        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                ReviewImages reviewImage = ReviewImages.builder()
                        .review(reviews)
                        .imageUrl(imageUrl)
                        .createdAt(LocalDateTime.now())
                        .build();

                savedImages.add(reviewImagesRepository.save(reviewImage));
            }
        }
        return ReviewResponseDTO.builder()
                .id(reviews.getId())
                .product(ProductSummaryDTO.from(product))
                .user(UserSummaryDTO.from(user))
                .rating(reviews.getRating())
                .content(reviews.getContent())
                .createdAt(reviews.getCreatedAt())
                .updatedAt(reviews.getUpdatedAt())
                .images(savedImages.stream()
                        .map(ReviewImages::getImageUrl)
                        .collect(Collectors.toList()))
                .likeCount(reviews.getLikeCount())
                .build();
    }

    public List<ReviewListDTO> getReviewLists(Long productId, SortType sortType, RatingSortType ratingSortType) {
        List<Reviews> reviews;

        if (ratingSortType != null && sortType != null) {
            if (ratingSortType == RatingSortType.DESC) {
                if (sortType == SortType.POPULAR) {
                    reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByRatingDescLikeCountDescCreatedAtDesc(productId);
                } else if (sortType == SortType.OLDEST) {
                    reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByRatingDescCreatedAtAsc(productId);
                } else {
                    reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByRatingDescCreatedAtDesc(productId);
                }
            } else {
                if (sortType == SortType.POPULAR) {
                    reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByRatingAscLikeCountDescCreatedAtDesc(productId);
                } else if (sortType == SortType.OLDEST) {
                    reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByRatingAscCreatedAtAsc(productId);
                } else {
                    reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByRatingAscCreatedAtDesc(productId);
                }
            }
        } else if (ratingSortType != null && sortType == null) {
            if (ratingSortType == RatingSortType.DESC) {
                reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByRatingDescCreatedAtDesc(productId);
            } else {
                reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByRatingAscCreatedAtDesc(productId);
            }
        } else if (ratingSortType == null  && sortType != null) {
            if (sortType == SortType.POPULAR) {
                reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc(productId);
            } else if (sortType == SortType.OLDEST) {
                reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByCreatedAtAsc(productId);
            } else {
                reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByCreatedAtDesc(productId);
            }
        } else {
            reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByCreatedAtDesc(productId);
        }

        // 이미지 변환 (첫번째 이미지만 넣음)
        return reviews.stream().map(review -> {
            String mainImage = null;
            if (review.getImages() != null && !review.getImages().isEmpty()) {
                mainImage = review.getImages().get(0).getImageUrl();
            }

            return ReviewListDTO.builder()
                    .id(review.getId())
                    .user(UserSummaryDTO.from(review.getUser()))
                    .rating(review.getRating())
                    .content(review.getContent())
                    .createdAt(review.getCreatedAt())
                    .image(mainImage)
                    .likeCount(review.getLikeCount())
                    .build();
        }).collect(Collectors.toList());
    }

    public ReviewResponseDTO getReview(Long reviewId) {
        Reviews reviews = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("해당 리뷰를 찾을 수 없습니다."));

        List<String> imageUrls = reviews.getImages().stream()
                .map(ReviewImages::getImageUrl)
                .collect(Collectors.toList());

        return ReviewResponseDTO.builder()
                .id(reviews.getId())
                .product(ProductSummaryDTO.from(reviews.getProduct()))
                .user(UserSummaryDTO.from(reviews.getUser()))
                .rating(reviews.getRating())
                .content(reviews.getContent())
                .createdAt(reviews.getCreatedAt())
                .updatedAt(reviews.getUpdatedAt())
                .images(imageUrls)
                .likeCount(reviews.getLikeCount())
                .build();
    }

    public ReviewResponseDTO patchReview(Long reviewId, ReviewRequest request) {
        Users user = authenticatedUserUtils.getCurrentUser();
        Reviews reviews = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("해당 리뷰를 찾을 수 없습니다."));
        // 소유자 확인
        if (!reviews.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("수정할 수 있는 권한이 없습니다.");
        }
        reviews.setRating(request.getRating());
        reviews.setContent(request.getContent());

        if (request.getImages()!=null) {
            // 기존 이미지 삭제
            reviews.getImages().clear();

            // 새 이미지 추가
            List<String> imageUrls = request.getImages();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    ReviewImages reviewImage = ReviewImages.builder()
                            .review(reviews)
                            .imageUrl(imageUrl)
                            .createdAt(LocalDateTime.now())
                            .build();
                    reviews.getImages().add(reviewImage);
                }
            }
        }

        reviewRepository.save(reviews);

        return ReviewResponseDTO.builder()
                .id(reviews.getId())
                .product(ProductSummaryDTO.from(reviews.getProduct()))
                .user(UserSummaryDTO.from(reviews.getUser()))
                .rating(reviews.getRating())
                .content(reviews.getContent())
                .createdAt(reviews.getCreatedAt())
                .updatedAt(reviews.getUpdatedAt())
                .images(
                        reviews.getImages().stream()
                                .map(ReviewImages::getImageUrl)
                                .collect(Collectors.toList())
                )
                .likeCount(reviews.getLikeCount())
                .build();
    }

    public ReviewDeleteDTO deleteReview(Long reviewId) {
        Users user = authenticatedUserUtils.getCurrentUser();
        Reviews reviews = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("해당 리뷰를 찾을 수 없습니다."));
        // 소유자 확인
        if(!reviews.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("삭제할 수 있는 권한이 없습니다.");
        }
        reviews.setDeletedAt(LocalDateTime.now());
        reviewRepository.save(reviews);

        String firstImage = reviews.getImages() != null && !reviews.getImages().isEmpty()
                ? reviews.getImages().get(0).getImageUrl() // 첫 번째 이미지 URL 사용
                : null;
        return ReviewDeleteDTO.builder()
                .products(ProductSummaryDTO.from(reviews.getProduct()))
                .rating(reviews.getRating())
                .content(reviews.getContent())
                .likeCount(reviews.getLikeCount())
                .deletedAt(reviews.getDeletedAt())
                .build();
    }

    public LikesResponseDTO likeReview(Long reviewId) {
        Users users = authenticatedUserUtils.getCurrentUser();

        Reviews reviews = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("해당 리뷰를 찾을 수 없습니다."));
        // 좋아요 여부 확인
        Optional<ReviewLikes> existing = reviewLikesRepository.findByUserAndReview(users, reviews);
        boolean liked;
        if (existing.isPresent()) {
            reviewLikesRepository.delete(existing.get());

            reviews.setLikeCount(reviews.getLikeCount() - 1);
            liked = false;
        } else {
            ReviewLikes reviewLikes = ReviewLikes.builder()
                    .review(reviews)
                    .user(users)
                    .build();
            reviewLikesRepository.save(reviewLikes);
            reviews.setLikeCount(reviews.getLikeCount() + 1);
            liked = true;
            // 알림 전송
            notificationService.sendReviewLikeNotification(reviews, users.getNickname());
        }
        reviewRepository.save(reviews);
        return new LikesResponseDTO(liked, reviews.getLikeCount());
    }
}
