package SpringClass.shop.service;

import SpringClass.shop.dto.Orders.request.OrderCreateRequest;
import SpringClass.shop.dto.Orders.request.OrderStatusUpdateRequest;
import SpringClass.shop.dto.Orders.response.*;
import SpringClass.shop.entity.Orders.OrderItems;
import SpringClass.shop.entity.Orders.Orders;
import SpringClass.shop.entity.Payments.Payments;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.NotificationType;
import SpringClass.shop.enums.OrderStatus;
import SpringClass.shop.exceptions.*;
import SpringClass.shop.repository.Orders.OrderItemsRepository;
import SpringClass.shop.repository.Orders.OrdersRepository;
import SpringClass.shop.repository.Payments.PaymentsRepository;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.repository.Sellers.SellersRepository;
import SpringClass.shop.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final SecurityUtils securityUtils;
    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final PaymentsRepository paymentsRepository;
    private final ProductsRepository productsRepository;
    private final SellersRepository sellersRepository;
    private final NotificationService notificationService;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        Users user = securityUtils.getCurrentUser();

        BigDecimal totalPrice = BigDecimal.ZERO;

        // 재고 확인 및 총 금액 계산
        for (OrderCreateRequest.OrderItemRequest itemReq : request.getOrderItems()) {
            Products product = productsRepository.findByIdAndDeletedAtIsNull(itemReq.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다. id=" + itemReq.getProductId()));

            if (product.getStock() < itemReq.getQuantity()) {
                throw new InsufficientStockException("재고가 부족합니다. 상품: " + product.getName());
            }

            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        // 주문 생성
        Orders order = Orders.builder()
                .user(user)
                .orderStatus(OrderStatus.WAITING)
                .totalPrice(totalPrice)
                .address(request.getAddress())
                .createdAt(LocalDateTime.now())
                .build();
        ordersRepository.save(order);

        // 주문 아이템 생성 & 재고 차감
        for (OrderCreateRequest.OrderItemRequest itemReq : request.getOrderItems()) {
            Products product = productsRepository.findByIdAndDeletedAtIsNull(itemReq.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));

            product.setStock(product.getStock() - itemReq.getQuantity());
            productsRepository.save(product);

            OrderItems orderItem = OrderItems.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .itemPrice(product.getPrice())
                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                    .build();
            orderItemsRepository.save(orderItem);

            // 판매자에게 주문 알림
            notificationService.send(
                    product.getSeller().getUser(),
                    NotificationType.ORDER_PLACED,
                    order.getId(),
                    user.getNickname() + "님이 " + product.getName() + "을(를) 주문했습니다."
            );
        }

        // 결제 레코드 생성 (PENDING)
        Payments payment = Payments.builder()
                .order(order)
                .user(user)
                .paymentMethod("PENDING")
                .paymentStatus("PENDING")
                .amount(totalPrice)
                .build();
        paymentsRepository.save(payment);

        return OrderCreateResponse.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(payment.getPaymentStatus())
                .build();
    }

    public Page<OrderListResponse> getMyOrders(Pageable pageable) {
        Users user = securityUtils.getCurrentUser();
        Page<Orders> orders = ordersRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return orders.map(order -> {
            String paymentStatus = paymentsRepository.findByOrder(order)
                    .map(Payments::getPaymentStatus)
                    .orElse("PENDING");

            List<OrderItemResponse> items = order.getItems().stream()
                    .map(OrderItemResponse::from)
                    .collect(Collectors.toList());

            return OrderListResponse.builder()
                    .orderId(order.getId())
                    .orderStatus(order.getOrderStatus())
                    .paymentStatus(paymentStatus)
                    .totalPrice(order.getTotalPrice())
                    .createdAt(order.getCreatedAt())
                    .items(items)
                    .build();
        });
    }

    public OrderDetailResponse getOrderDetail(Long orderId) {
        Users user = securityUtils.getCurrentUser();

        Orders order = ordersRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없거나 접근 권한이 없습니다."));

        String paymentStatus = paymentsRepository.findByOrder(order)
                .map(Payments::getPaymentStatus)
                .orElse("PENDING");

        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());

        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .address(order.getAddress())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(paymentStatus)
                .totalPrice(order.getTotalPrice())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Users user = securityUtils.getCurrentUser();

        Orders order = ordersRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없거나 접근 권한이 없습니다."));

        if (order.getOrderStatus() != OrderStatus.WAITING) {
            throw new OrderCancelNotAllowedException("WAITING 상태의 주문만 취소할 수 있습니다.");
        }

        // 재고 복구
        for (OrderItems item : order.getItems()) {
            Products product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productsRepository.save(product);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        ordersRepository.save(order);
    }

    @Transactional
    public OrderStatusUpdateResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Users user = securityUtils.getCurrentUser();

        Sellers seller = sellersRepository.findByUser(user)
                .orElseThrow(() -> new ForbiddenException("판매자만 배송 상태를 변경할 수 있습니다."));

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));

        boolean ownsOrderItem = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getSeller().getId().equals(seller.getId()));
        if (!ownsOrderItem) {
            throw new ForbiddenException("해당 주문에 대한 권한이 없습니다.");
        }

        OrderStatus newStatus = request.getOrderStatus();

        if (newStatus == OrderStatus.SHIPPED) {
            order.setShippedAt(LocalDateTime.now());
            notificationService.send(
                    order.getUser(),
                    NotificationType.ORDER_SHIPPED,
                    order.getId(),
                    "주문하신 상품이 발송되었습니다."
            );
        } else if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            notificationService.send(
                    order.getUser(),
                    NotificationType.PURCHASE_COMPLETE,
                    order.getId(),
                    "주문하신 상품이 배달 완료되었습니다."
            );
        } else {
            throw new ForbiddenException("SHIPPED 또는 DELIVERED 상태로만 변경할 수 있습니다.");
        }

        order.setOrderStatus(newStatus);
        ordersRepository.save(order);

        return OrderStatusUpdateResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }
}
