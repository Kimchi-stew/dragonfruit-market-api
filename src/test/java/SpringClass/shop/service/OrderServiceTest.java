package SpringClass.shop.service;

import SpringClass.shop.dto.Orders.request.OrderCreateRequest;
import SpringClass.shop.dto.Orders.request.OrderStatusUpdateRequest;
import SpringClass.shop.dto.Orders.response.OrderCreateResponse;
import SpringClass.shop.dto.Orders.response.OrderDetailResponse;
import SpringClass.shop.dto.Orders.response.OrderStatusUpdateResponse;
import SpringClass.shop.entity.Orders.OrderItems;
import SpringClass.shop.entity.Orders.Orders;
import SpringClass.shop.entity.Payments.Payments;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.NotificationType;
import SpringClass.shop.enums.OrderStatus;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.common.ForbiddenException;
import SpringClass.shop.exceptions.order.InsufficientStockException;
import SpringClass.shop.exceptions.order.OrderCancelNotAllowedException;
import SpringClass.shop.exceptions.order.OrderNotFoundException;
import SpringClass.shop.exceptions.product.ProductNotFoundException;
import SpringClass.shop.repository.Coupon.UserCouponsRepository;
import SpringClass.shop.repository.Orders.OrderItemsRepository;
import SpringClass.shop.repository.Orders.OrdersRepository;
import SpringClass.shop.repository.Payments.PaymentsRepository;
import SpringClass.shop.repository.Products.ProductsRepository;
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
class OrderServiceTest {

    @Mock private SecurityUtils securityUtils;
    @Mock private UserCouponsRepository userCouponsRepository;
    @Mock private OrdersRepository ordersRepository;
    @Mock private OrderItemsRepository orderItemsRepository;
    @Mock private PaymentsRepository paymentsRepository;
    @Mock private ProductsRepository productsRepository;
    @Mock private SellersRepository sellersRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private OrderService orderService;

    private Users buyer;
    private Users sellerUser;
    private Sellers seller;
    private Products product;

    @BeforeEach
    void setUp() {
        buyer = Users.builder()
                .id(1L).email("buyer@t.com").nickname("구매자")
                .gender(GenderRole.M).userRole(UserRole.USER).build();

        sellerUser = Users.builder()
                .id(2L).email("seller@t.com").nickname("판매자")
                .gender(GenderRole.M).userRole(UserRole.SELLER).build();

        seller = Sellers.builder().id(1L).user(sellerUser).storeName("테스트샵").build();

        product = Products.builder()
                .id(1L).name("상품").price(new BigDecimal("10000"))
                .stock(10).likeCount(0).seller(seller).build();
    }

    @Test
    @DisplayName("쿠폰 없이 주문 생성 성공")
    void createOrder_noCoupon_success() {
        OrderCreateRequest.OrderItemRequest itemReq = new OrderCreateRequest.OrderItemRequest(1L, 2);
        OrderCreateRequest request = OrderCreateRequest.builder()
                .address("서울시").orderItems(List.of(itemReq)).couponId(null).build();

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(productsRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        doNothing().when(notificationService).send(any(), any(), any(), any());

        OrderCreateResponse response = orderService.createOrder(request);

        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.WAITING);
        assertThat(response.getTotalPrice()).isEqualByComparingTo("20000");
        assertThat(response.getPaymentStatus()).isEqualTo("PENDING");
        verify(ordersRepository).save(any(Orders.class));
        verify(orderItemsRepository).save(any(OrderItems.class));
        verify(paymentsRepository).save(any(Payments.class));
    }

    @Test
    @DisplayName("재고 부족 시 InsufficientStockException 발생")
    void createOrder_insufficientStock_throwsException() {
        Products lowStockProduct = Products.builder()
                .id(1L).name("품절상품").price(new BigDecimal("5000")).stock(1).seller(seller).build();

        OrderCreateRequest.OrderItemRequest itemReq = new OrderCreateRequest.OrderItemRequest(1L, 5);
        OrderCreateRequest request = OrderCreateRequest.builder()
                .address("서울시").orderItems(List.of(itemReq)).couponId(null).build();

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(productsRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(lowStockProduct));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("존재하지 않는 상품 주문 시 ProductNotFoundException 발생")
    void createOrder_productNotFound_throwsException() {
        OrderCreateRequest.OrderItemRequest itemReq = new OrderCreateRequest.OrderItemRequest(999L, 1);
        OrderCreateRequest request = OrderCreateRequest.builder()
                .address("서울시").orderItems(List.of(itemReq)).couponId(null).build();

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(productsRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("WAITING 상태의 주문 취소 성공 및 재고 복구")
    void cancelOrder_waiting_success() {
        Products productForItem = Products.builder()
                .id(1L).name("상품").price(new BigDecimal("10000")).stock(8).seller(seller).build();
        OrderItems item = OrderItems.builder()
                .id(1L).product(productForItem).quantity(2).build();
        Orders order = Orders.builder()
                .id(1L).user(buyer).orderStatus(OrderStatus.WAITING)
                .totalPrice(new BigDecimal("20000")).address("서울")
                .createdAt(LocalDateTime.now()).items(List.of(item)).build();

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(ordersRepository.findByIdAndUser(1L, buyer)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(productForItem.getStock()).isEqualTo(10);
        verify(ordersRepository).save(order);
    }

    @Test
    @DisplayName("WAITING이 아닌 주문 취소 시 OrderCancelNotAllowedException 발생")
    void cancelOrder_notWaiting_throwsException() {
        Orders order = Orders.builder()
                .id(1L).user(buyer).orderStatus(OrderStatus.SHIPPED)
                .totalPrice(new BigDecimal("10000")).address("서울")
                .createdAt(LocalDateTime.now()).build();

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(ordersRepository.findByIdAndUser(1L, buyer)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(OrderCancelNotAllowedException.class);
    }

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_success() {
        OrderItems item = OrderItems.builder()
                .id(1L).product(product).quantity(1)
                .itemPrice(new BigDecimal("10000")).totalPrice(new BigDecimal("10000")).build();
        Orders order = Orders.builder()
                .id(1L).user(buyer).orderStatus(OrderStatus.WAITING)
                .totalPrice(new BigDecimal("10000")).address("서울")
                .createdAt(LocalDateTime.now()).items(List.of(item)).build();

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(ordersRepository.findByIdAndUser(1L, buyer)).thenReturn(Optional.of(order));
        when(paymentsRepository.findByOrder(order)).thenReturn(Optional.empty());

        OrderDetailResponse response = orderService.getOrderDetail(1L);

        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.WAITING);
        assertThat(response.getPaymentStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("주문 상세 조회 시 주문 없으면 OrderNotFoundException 발생")
    void getOrderDetail_notFound_throwsException() {
        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(ordersRepository.findByIdAndUser(999L, buyer)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderDetail(999L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("판매자가 주문 상태를 SHIPPED로 변경")
    void updateOrderStatus_shipped_success() {
        OrderItems item = OrderItems.builder()
                .id(1L).product(product).quantity(1).build();
        Orders order = Orders.builder()
                .id(1L).user(buyer).orderStatus(OrderStatus.WAITING)
                .totalPrice(new BigDecimal("10000")).address("서울")
                .createdAt(LocalDateTime.now()).items(List.of(item)).build();

        when(securityUtils.getCurrentUser()).thenReturn(sellerUser);
        when(sellersRepository.findByUser(sellerUser)).thenReturn(Optional.of(seller));
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(notificationService).send(any(), any(), any(), any());

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.SHIPPED);
        OrderStatusUpdateResponse response = orderService.updateOrderStatus(1L, request);

        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(response.getShippedAt()).isNotNull();
    }

    @Test
    @DisplayName("판매자가 아니면 주문 상태 변경 불가")
    void updateOrderStatus_notSeller_throwsException() {
        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(sellersRepository.findByUser(buyer)).thenReturn(Optional.empty());

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.SHIPPED);

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, request))
                .isInstanceOf(ForbiddenException.class);
    }
}
