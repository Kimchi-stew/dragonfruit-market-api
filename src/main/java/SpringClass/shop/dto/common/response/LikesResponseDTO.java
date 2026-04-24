package SpringClass.shop.dto.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LikesResponseDTO {
    private boolean liked;
    private int likeCount;
}
