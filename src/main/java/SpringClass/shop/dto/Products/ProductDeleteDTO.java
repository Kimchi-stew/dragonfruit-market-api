package SpringClass.shop.dto.Products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProductDeleteDTO {
    private String name;
    private String image;
    private LocalDateTime deletedAt;
}
