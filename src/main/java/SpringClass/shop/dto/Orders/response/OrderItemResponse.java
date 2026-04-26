package SpringClass.shop.dto.Orders.response;

import SpringClass.shop.entity.Orders.OrderItems;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal itemPrice;
    private BigDecimal totalPrice;

    public static OrderItemResponse from(OrderItems item) {
        return OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .itemPrice(item.getItemPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
