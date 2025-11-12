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
public class SellerDeleteDTO {
    private String storeName;
    private String image;
    private LocalDateTime deletedAt;
}
