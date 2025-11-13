package SpringClass.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProductListDTO {
    private Long id;
    private SellerSummaryDTO seller;
    private String name;
    private BigDecimal price;
    private String image; // 대표 이미지 1개
    private int likeCount;
}
