package SpringClass.shop.dto.Sellers.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SellerFollowDTO {
    private boolean followed;
}
