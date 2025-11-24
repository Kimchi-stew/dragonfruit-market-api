package SpringClass.shop.dto.Cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CartRequest {
    private Long productId;
    private int quantity;
}
