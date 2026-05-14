package SpringClass.shop.service;

import SpringClass.shop.dto.Cart.request.CartRequest;
import SpringClass.shop.dto.Cart.response.CartResponse;
import SpringClass.shop.dto.Products.response.ProductSummaryDTO;
import SpringClass.shop.entity.Cart.CartItems;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.cart.CartNotFoundException;
import SpringClass.shop.exceptions.product.ProductNotFoundException;
import SpringClass.shop.repository.Cart.CartItemsRepository;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private SecurityUtils SecurityUtils;
    @Mock private ProductsRepository productsRepository;
    @Mock private CartItemsRepository cartItemsRepository;

    @InjectMocks private CartService cartService;

    private Users user;
    private Sellers seller;
    private Products product;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .id(1L).email("u@t.com").nickname("유저")
                .gender(GenderRole.M).userRole(UserRole.USER).build();

        seller = Sellers.builder().id(1L).storeName("테스트샵").image("img.jpg").build();

        product = Products.builder()
                .id(1L).name("상품").price(new BigDecimal("10000"))
                .stock(10).likeCount(0).seller(seller).build();
    }

    @Test
    @DisplayName("장바구니에 없는 상품 추가 시 새 CartItem 생성")
    void plusProduct_newItem_createsCartItem() {
        CartRequest request = new CartRequest(1L, 2);
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(productsRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(cartItemsRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

        ProductSummaryDTO result = cartService.plusProduct(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("상품");
        verify(cartItemsRepository).save(any(CartItems.class));
    }

    @Test
    @DisplayName("장바구니에 이미 있는 상품 추가 시 수량 증가")
    void plusProduct_existingItem_increasesQuantity() {
        CartRequest request = new CartRequest(1L, 3);
        CartItems existing = CartItems.builder()
                .id(1L).user(user).product(product).quantity(2).build();

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(productsRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(cartItemsRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(existing));

        cartService.plusProduct(request);

        assertThat(existing.getQuantity()).isEqualTo(5);
        verify(cartItemsRepository).save(existing);
    }

    @Test
    @DisplayName("존재하지 않는 상품 추가 시 ProductNotFoundException 발생")
    void plusProduct_productNotFound_throwsException() {
        CartRequest request = new CartRequest(999L, 1);
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(productsRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.plusProduct(request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("장바구니 조회 성공 시 총 수량, 총 금액 계산하여 반환")
    void getCart_success_returnsCartWithTotals() {
        CartItems item = CartItems.builder()
                .id(1L).user(user).product(product).quantity(2).build();

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(cartItemsRepository.findByUser(user)).thenReturn(List.of(item));

        CartResponse response = cartService.getCart();

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalQuantity()).isEqualTo(2);
        assertThat(response.getTotalPrice()).isEqualByComparingTo("20000");
    }

    @Test
    @DisplayName("장바구니가 비어있을 때 CartNotFoundException 발생")
    void getCart_empty_throwsException() {
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(cartItemsRepository.findByUser(user)).thenReturn(List.of());

        assertThatThrownBy(() -> cartService.getCart())
                .isInstanceOf(CartNotFoundException.class);
    }

    @Test
    @DisplayName("요청 수량보다 장바구니 수량이 많으면 수량 감소")
    void deleteProduct_decrementQuantity() {
        CartRequest request = new CartRequest(1L, 1);
        CartItems existing = CartItems.builder()
                .id(1L).user(user).product(product).quantity(3).build();

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(productsRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(cartItemsRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(existing));

        cartService.deleteProduct(request);

        assertThat(existing.getQuantity()).isEqualTo(2);
        verify(cartItemsRepository).save(existing);
        verify(cartItemsRepository, never()).delete(any());
    }

    @Test
    @DisplayName("요청 수량이 장바구니 수량 이상이면 장바구니에서 삭제")
    void deleteProduct_removeCartItem() {
        CartRequest request = new CartRequest(1L, 3);
        CartItems existing = CartItems.builder()
                .id(1L).user(user).product(product).quantity(2).build();

        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(productsRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(cartItemsRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(existing));

        cartService.deleteProduct(request);

        verify(cartItemsRepository).delete(existing);
        verify(cartItemsRepository, never()).save(any());
    }

    @Test
    @DisplayName("장바구니에 없는 상품 삭제 요청 시 CartNotFoundException 발생")
    void deleteProduct_notInCart_throwsException() {
        CartRequest request = new CartRequest(1L, 1);
        when(SecurityUtils.getCurrentUser()).thenReturn(user);
        when(productsRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(cartItemsRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteProduct(request))
                .isInstanceOf(CartNotFoundException.class);
    }
}
