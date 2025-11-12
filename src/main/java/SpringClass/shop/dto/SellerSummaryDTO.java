package SpringClass.shop.dto;

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
}
