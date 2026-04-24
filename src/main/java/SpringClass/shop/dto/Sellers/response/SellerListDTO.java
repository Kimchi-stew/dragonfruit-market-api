package SpringClass.shop.dto.Sellers.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SellerListDTO {
    private Long id;
    private String storeName;
    private String image;
    private int likeCount;
    private int followCount;
}
