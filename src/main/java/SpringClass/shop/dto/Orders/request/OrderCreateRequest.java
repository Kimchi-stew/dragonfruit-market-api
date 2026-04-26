package SpringClass.shop.dto.Orders.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrderCreateRequest {
    private String address;
    private List<OrderItemRequest> orderItems;
    private Long couponId;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}
