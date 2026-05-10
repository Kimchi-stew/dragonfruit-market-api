package SpringClass.shop.service;

import SpringClass.shop.dto.Products.response.ProductResponse;
import SpringClass.shop.dto.Products.response.RecommendProductDTO;
import SpringClass.shop.entity.Products.*;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.product.ProductNotFoundException;
import SpringClass.shop.repository.Categories.CategoriesRepository;
import SpringClass.shop.repository.Products.*;
import SpringClass.shop.repository.Reviews.ReviewRepository;
import SpringClass.shop.repository.Sellers.SellersRepository;
import SpringClass.shop.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private SecurityUtils securityUtils;
    @Mock private FileService fileService;
    @Mock private SellersRepository sellersRepository;
    @Mock private ProductsRepository productsRepository;
    @Mock private ProductLikeRepository productLikeRepository;
    @Mock private ProductWishRepository productWishRepository;
    @Mock private CategoriesRepository categoriesRepository;
    @Mock private ProductCategoriesRepository productCategoriesRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private UserBehaviorLogRepository userBehaviorLogRepository;
    @Mock private UserRecommendationRepository userRecommendationRepository;

    @InjectMocks private ProductService productService;

    private Users user;
    private Sellers seller;
    private Products product;
    private ProductCategories productCategories;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .id(1L).email("user@test.com").password("pw")
                .nickname("유저").gender(GenderRole.M).userRole(UserRole.USER).build();

        Users sellerUser = Users.builder()
                .id(2L).email("seller@test.com").password("pw")
                .nickname("판매자").gender(GenderRole.M).userRole(UserRole.SELLER).build();

        seller = Sellers.builder().id(1L).user(sellerUser).storeName("테스트샵").build();

        product = Products.builder()
                .id(1L).name("테스트 상품").price(new BigDecimal("10000"))
                .stock(5).likeCount(3).seller(seller).build();

        productCategories = mock(ProductCategories.class);
    }

    private void stubProductCategories() {
        var category = mock(SpringClass.shop.entity.Categories.Categories.class);
        when(category.getName()).thenReturn("의류");
        when(productCategories.getCategory()).thenReturn(category);
        when(productCategoriesRepository.findByProduct(product)).thenReturn(Optional.of(productCategories));
    }

    // ── getProduct ────────────────────────────────────────────────────

    @Test
    @DisplayName("로그인 유저 상품 조회 시 VIEW 로그 저장 및 정상 응답")
    void getProduct_loggedIn_savesViewLog() {
        stubProductCategories();
        when(securityUtils.getCurrentUserOptional()).thenReturn(Optional.of(user));
        when(productsRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(userBehaviorLogRepository.save(any())).thenReturn(null);
        when(productWishRepository.existsByUserAndProduct(user, product)).thenReturn(true);
        when(reviewRepository.findAverageRating(1L)).thenReturn(4.5);

        ProductResponse response = productService.getProduct(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("테스트 상품");
        assertThat(response.isWished()).isTrue();
        verify(userBehaviorLogRepository).save(argThat(log ->
                log.getBehaviorType().equals("VIEW") && log.getUser().equals(user)
        ));
    }

    @Test
    @DisplayName("비로그인 상품 조회 시 로그 저장 생략, wished=false")
    void getProduct_notLoggedIn_skipsLogAndWishedFalse() {
        stubProductCategories();
        when(securityUtils.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productsRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.findAverageRating(1L)).thenReturn(null);

        ProductResponse response = productService.getProduct(1L);

        assertThat(response.isWished()).isFalse();
        verify(userBehaviorLogRepository, never()).save(any());
        verify(productWishRepository, never()).existsByUserAndProduct(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 ProductNotFoundException 발생")
    void getProduct_notFound_throwsProductNotFoundException() {
        when(securityUtils.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productsRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("상품을 찾을 수 없습니다.");
    }

    // ── getRecommendations ────────────────────────────────────────────

    @Test
    @DisplayName("추천 데이터 있는 로그인 유저는 AI 추천 상품 반환")
    void getRecommendations_withRecommendations_returnsAiRecommendations() {
        UserRecommendation rec = UserRecommendation.builder()
                .id(new UserRecommendationId(1L, 1L))
                .user(user).product(product).score(8.0)
                .createdAt(LocalDateTime.now()).build();

        when(securityUtils.getCurrentUserOptional()).thenReturn(Optional.of(user));
        when(userRecommendationRepository.findByUserOrderByScoreDesc(eq(user), any(Pageable.class)))
                .thenReturn(List.of(rec));

        List<RecommendProductDTO> result = productService.getRecommendations(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(1L);
        assertThat(result.get(0).getScore()).isEqualTo(8.0);
        verify(productsRepository, never()).findAllByDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc(any());
    }

    @Test
    @DisplayName("추천 데이터 없는 로그인 유저는 인기순 fallback 반환")
    void getRecommendations_loggedInNoData_returnsPopularFallback() {
        when(securityUtils.getCurrentUserOptional()).thenReturn(Optional.of(user));
        when(userRecommendationRepository.findByUserOrderByScoreDesc(eq(user), any(Pageable.class)))
                .thenReturn(List.of());
        when(productsRepository.findAllByDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product)));

        List<RecommendProductDTO> result = productService.getRecommendations(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(1L);
        assertThat(result.get(0).getScore()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("비로그인 유저는 인기순 fallback 반환")
    void getRecommendations_notLoggedIn_returnsPopularFallback() {
        when(securityUtils.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productsRepository.findAllByDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product)));

        List<RecommendProductDTO> result = productService.getRecommendations(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScore()).isEqualTo(0.0);
        verify(userRecommendationRepository, never()).findByUserOrderByScoreDesc(any(), any());
    }

    @Test
    @DisplayName("추천 결과가 없고 인기 상품도 없으면 빈 리스트 반환")
    void getRecommendations_noData_returnsEmptyList() {
        when(securityUtils.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productsRepository.findAllByDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<RecommendProductDTO> result = productService.getRecommendations(10);

        assertThat(result).isEmpty();
    }
}
