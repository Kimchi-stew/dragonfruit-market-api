package SpringClass.shop.service;

import SpringClass.shop.dto.Products.response.AiProductContextResponse;
import SpringClass.shop.entity.Categories.Categories;
import SpringClass.shop.entity.Products.ProductCategories;
import SpringClass.shop.entity.Products.ProductImages;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.product.ProductNotFoundException;
import SpringClass.shop.repository.Products.ProductCategoriesRepository;
import SpringClass.shop.repository.Products.ProductsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiProductServiceTest {

    @Mock private ProductsRepository productsRepository;
    @Mock private ProductCategoriesRepository productCategoriesRepository;

    @InjectMocks private AiProductService aiProductService;

    private Users sellerUser;
    private Sellers seller;
    private Products product;
    private Categories category;

    @BeforeEach
    void setUp() {
        sellerUser = Users.builder()
                .id(1L).email("seller@test.com").password("pw")
                .nickname("판매자").gender(GenderRole.M).userRole(UserRole.SELLER).build();

        seller = Sellers.builder().id(1L).user(sellerUser).storeName("테스트샵").build();

        category = Categories.builder().id(1L).name("의류").build();

        ProductImages image = ProductImages.builder()
                .id(1L).imageUrl("https://example.com/img.jpg")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        product = Products.builder()
                .id(1L).name("테스트 상품").price(new BigDecimal("29900"))
                .stock(10).description("상품 설명").likeCount(5)
                .seller(seller).images(List.of(image)).build();
    }

    // ── getProductContext ─────────────────────────────────────────────

    @Test
    @DisplayName("상품 컨텍스트 조회 성공 시 모든 필드 포함된 응답 반환")
    void getProductContext_success_returnsAllFields() {
        ProductCategories pc = mock(ProductCategories.class);
        when(pc.getCategory()).thenReturn(category);

        when(productsRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(productCategoriesRepository.findAllByProduct(product)).thenReturn(List.of(pc));

        AiProductContextResponse response = aiProductService.getProductContext(1L);

        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("테스트 상품");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("29900"));
        assertThat(response.getStock()).isEqualTo(10);
        assertThat(response.getDescription()).isEqualTo("상품 설명");
        assertThat(response.getSellerName()).isEqualTo("테스트샵");
        assertThat(response.getCategories()).containsExactly("의류");
        assertThat(response.getImages()).containsExactly("https://example.com/img.jpg");
    }

    @Test
    @DisplayName("카테고리와 이미지가 없어도 빈 리스트로 응답")
    void getProductContext_noImagesAndCategories_returnsEmptyLists() {
        Products emptyProduct = Products.builder()
                .id(2L).name("이미지 없는 상품").price(new BigDecimal("5000"))
                .stock(3).description(null).likeCount(0)
                .seller(seller).images(null).build();

        when(productsRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(emptyProduct));
        when(productCategoriesRepository.findAllByProduct(emptyProduct)).thenReturn(List.of());

        AiProductContextResponse response = aiProductService.getProductContext(2L);

        assertThat(response.getCategories()).isEmpty();
        assertThat(response.getImages()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID로 조회 시 ProductNotFoundException 발생")
    void getProductContext_notFound_throwsProductNotFoundException() {
        when(productsRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiProductService.getProductContext(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("상품을 찾을 수 없습니다.");
    }
}
