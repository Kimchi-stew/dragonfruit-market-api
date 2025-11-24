package SpringClass.shop.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
