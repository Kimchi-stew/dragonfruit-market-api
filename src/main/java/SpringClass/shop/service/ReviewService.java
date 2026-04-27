package SpringClass.shop.service;

import SpringClass.shop.dto.common.response.LikesResponseDTO;
import SpringClass.shop.dto.Products.response.ProductSummaryDTO;
import SpringClass.shop.dto.Reviews.request.ReviewRequest;
import SpringClass.shop.dto.Reviews.response.ReviewDeleteDTO;
import SpringClass.shop.dto.Reviews.response.ReviewListDTO;
import SpringClass.shop.dto.Reviews.response.ReviewResponseDTO;
import SpringClass.shop.dto.Users.response.UserSummaryDTO;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Reviews.ReviewImages;
import SpringClass.shop.entity.Reviews.ReviewLikes;
import SpringClass.shop.entity.Reviews.Reviews;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.RatingSortType;
import SpringClass.shop.enums.SortType;
import SpringClass.shop.exceptions.ForbiddenException;
import SpringClass.shop.exceptions.ReviewNotFoundException;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.repository.Reviews.ReviewImagesRepository;
import SpringClass.shop.repository.Reviews.ReviewLikesRepository;
import SpringClass.shop.repository.Reviews.ReviewRepository;
import SpringClass.shop.security.SecurityUtils;
import SpringClass.shop.service.FileService;
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
    private final SecurityUtils SecurityUtils;
    private final FileService fileService;
    private final ReviewRepository reviewRepository;
    private final ProductsRepository productsRepository;
    private final ReviewImagesRepository reviewImagesRepository;
    private final ReviewLikesRepository reviewLikesRepository;
    private final NotificationService notificationService;

    public ReviewResponseDTO createReview(Long productId, ReviewRequest request) {
        Users user = SecurityUtils.getCurrentUser();
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

        // 이미지 저장 및 medias entityId 연결
        List<String> imageUrls = fileService.getUrlsByIds(request.getMediaIds());
        List<ReviewImages> savedImages = new ArrayList<>();
        if (!imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                ReviewImages reviewImage = ReviewImages.builder()
                        .review(reviews)
                        .imageUrl(imageUrl)
                        .createdAt(LocalDateTime.now())
                        .build();
                savedImages.add(reviewImagesRepository.save(reviewImage));
            }
            fileService.linkMedias(request.getMediaIds(), reviews.getId());
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
        Users user = SecurityUtils.getCurrentUser();
        Reviews reviews = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("해당 리뷰를 찾을 수 없습니다."));
        // 소유자 확인
        if (!reviews.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("수정할 수 있는 권한이 없습니다.");
        }
        reviews.setRating(request.getRating());
        reviews.setContent(request.getContent());

        if (request.getMediaIds() != null) {
            reviews.getImages().clear();

            List<String> imageUrls = fileService.getUrlsByIds(request.getMediaIds());
            if (!imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    ReviewImages reviewImage = ReviewImages.builder()
                            .review(reviews)
                            .imageUrl(imageUrl)
                            .createdAt(LocalDateTime.now())
                            .build();
                    reviews.getImages().add(reviewImage);
                }
                fileService.linkMedias(request.getMediaIds(), reviews.getId());
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
        Users user = SecurityUtils.getCurrentUser();
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
        Users users = SecurityUtils.getCurrentUser();

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
