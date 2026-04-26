package SpringClass.shop.dto.Orders.response;

import SpringClass.shop.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrderListResponse {
    private Long orderId;
    private OrderStatus orderStatus;
    private String paymentStatus;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
