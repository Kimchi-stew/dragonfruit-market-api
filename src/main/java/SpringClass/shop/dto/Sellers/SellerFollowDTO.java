package SpringClass.shop.dto.Sellers;

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
