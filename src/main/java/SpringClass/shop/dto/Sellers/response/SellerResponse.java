package SpringClass.shop.dto.Sellers.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SellerResponse {
    private Long id;
    private Long userId;
    private String storeName;
    private String description;
    private String image;
    private LocalDateTime createdAt;
    private int likeCount;
    private int followCount;
    private boolean followed;
}
