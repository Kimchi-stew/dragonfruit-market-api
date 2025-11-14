package SpringClass.shop.dto;

import SpringClass.shop.entity.Sellers.Sellers;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerSummaryDTO {
    private Long id;
    private String storeName;
    private String image;

    public static SellerSummaryDTO from(Sellers sellers) {
        return SellerSummaryDTO.builder()
                .id(sellers.getId())
                .storeName(sellers.getStoreName())
                .image(sellers.getImage())
                .build();
    }
}
