package SpringClass.shop.dto.Orders.response;

import SpringClass.shop.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrderCreateResponse {
    private Long orderId;
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    private String paymentStatus;
}
