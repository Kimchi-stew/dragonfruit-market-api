package SpringClass.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
