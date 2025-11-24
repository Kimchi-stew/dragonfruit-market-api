package SpringClass.shop.dto.Reviews;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
