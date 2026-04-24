package SpringClass.shop.dto.Cart.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CartResponse {
    private List<CartItemResponse> items;  // 장바구니 아이템 리스트
    private int totalQuantity;             // 전체 수량
    private BigDecimal totalPrice;         // 전체 가격
}
