package SpringClass.shop.dto;

import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReviewRequest {
    private Integer rating;
    private String content;
    private List<String> images;
}
