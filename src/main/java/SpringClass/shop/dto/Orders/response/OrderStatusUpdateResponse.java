package SpringClass.shop.dto.Orders.response;

import SpringClass.shop.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrderStatusUpdateResponse {
    private Long orderId;
    private OrderStatus orderStatus;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}
