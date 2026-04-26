package SpringClass.shop.dto.Orders.request;

import SpringClass.shop.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderStatusUpdateRequest {
    private OrderStatus orderStatus;
}
