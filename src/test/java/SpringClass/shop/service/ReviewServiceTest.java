package SpringClass.shop.service;

import SpringClass.shop.dto.Reviews.request.ReviewRequest;
import SpringClass.shop.dto.Reviews.response.ReviewDeleteDTO;
import SpringClass.shop.dto.Reviews.response.ReviewResponseDTO;
import SpringClass.shop.dto.common.response.LikesResponseDTO;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Reviews.ReviewImages;
import SpringClass.shop.entity.Reviews.ReviewLikes;
import SpringClass.shop.entity.Reviews.Reviews;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.common.ForbiddenException;
import SpringClass.shop.exceptions.review.ReviewNotFoundException;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.repository.Reviews.ReviewImagesRepository;
import SpringClass.shop.repository.Reviews.ReviewLikesRepository;
import SpringClass.shop.repository.Reviews.ReviewRepository;
import SpringClass.shop.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private SecurityUtils SecurityUtils;
    @Mock private FileService fileService;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductsRepository productsRepository;
    @Mock private ReviewImagesRepository reviewImagesRepository;
    @Mock private ReviewLikesRepository reviewLikesRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private ReviewService reviewService;

    private Users user;
    private Users otherUser;
    private Sellers seller;
    private Products product;
    private Reviews review;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .id(1L).email("u@t.com").nickname("유저")
                .gender(GenderRole.M).userRole(UserRole.USER).build();

        otherUser = Users.builder()
                .id(2L).email("other@t.com").nickname("타인")
                .gender(GenderRole.M).userRole(UserRole.USER).build();

        seller = Sellers.builder().id(1L).storeName("샵").image("img.jpg").build();
        product = Products.builder()
                .id(1L).name("상품").price(new BigDecimal("10000")).stock(5).seller(seller).build();

        review = Reviews.builder()
                .id(1L).product(product).user(user)
                .rating(5).content("좋아요").likeCount(0)
                .createdAt(LocalDateTime.now()).images(new ArrayList<>()).build();
    }

    @Test
    @DisplayName("리뷰 생성 성공 (이미지 없음)")
    void createReview_noImages_success() {
        ReviewRequest request = mock(ReviewRequest.class);
        when(request.getRating()).thenReturn(5);
        when(request.getContent()).thenReturn("좋아요");
        when(request.getMediaIds()).thenReturn(List.of());

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(productsRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.save(any())).thenReturn(review);
        when(fileService.getUrlsByIds(List.of())).thenReturn(List.of());

        ReviewResponseDTO result = reviewService.createReview(1L, request);

        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getContent()).isEqualTo("좋아요");
    }

    @Test
    @DisplayName("리뷰 단건 조회 성공")
    void getReview_found_success() {
        when(reviewRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(review));

        ReviewResponseDTO result = reviewService.getReview(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("좋아요");
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 조회 시 ReviewNotFoundException 발생")
    void getReview_notFound_throwsException() {
        when(reviewRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReview(999L))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    @DisplayName("리뷰 수정 성공 (소유자)")
    void patchReview_owner_success() {
        ReviewRequest request = mock(ReviewRequest.class);
        when(request.getRating()).thenReturn(4);
        when(request.getContent()).thenReturn("수정됨");
        when(request.getMediaIds()).thenReturn(null);

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(reviewRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(review));

        ReviewResponseDTO result = reviewService.patchReview(1L, request);

        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getContent()).isEqualTo("수정됨");
        verify(reviewRepository).save(review);
    }

    @Test
    @DisplayName("리뷰 수정 시 소유자가 아니면 ForbiddenException 발생")
    void patchReview_notOwner_throwsException() {
        ReviewRequest request = mock(ReviewRequest.class);
        when(SecurityUtils.getCurrentUser()).thenReturn(otherUser);
        when(reviewRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.patchReview(1L, request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("리뷰 삭제 성공 (소유자)")
    void deleteReview_owner_success() {
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(reviewRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(review));

        ReviewDeleteDTO result = reviewService.deleteReview(1L);

        assertThat(review.getDeletedAt()).isNotNull();
        verify(reviewRepository).save(review);
    }

    @Test
    @DisplayName("리뷰 삭제 시 소유자가 아니면 ForbiddenException 발생")
    void deleteReview_notOwner_throwsException() {
        when(SecurityUtils.getCurrentUser()).thenReturn(otherUser);
        when(reviewRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.deleteReview(1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("리뷰 좋아요 추가")
    void likeReview_addLike_success() {
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(reviewRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(review));
        when(reviewLikesRepository.findByUserAndReview(user, review)).thenReturn(Optional.empty());
        doNothing().when(notificationService).sendReviewLikeNotification(any(), any());

        LikesResponseDTO result = reviewService.likeReview(1L);

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getLikeCount()).isEqualTo(1);
        verify(reviewLikesRepository).save(any(ReviewLikes.class));
    }

    @Test
    @DisplayName("이미 좋아요한 리뷰 다시 좋아요 시 취소")
    void likeReview_removeLike_success() {
        ReviewLikes existing = ReviewLikes.builder().review(review).user(user).build();
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(reviewRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(review));
        when(reviewLikesRepository.findByUserAndReview(user, review)).thenReturn(Optional.of(existing));

        LikesResponseDTO result = reviewService.likeReview(1L);

        assertThat(result.isLiked()).isFalse();
        assertThat(result.getLikeCount()).isEqualTo(-1);
        verify(reviewLikesRepository).delete(existing);
    }
}
