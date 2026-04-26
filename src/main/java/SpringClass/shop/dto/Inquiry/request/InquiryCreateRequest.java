package SpringClass.shop.dto.Inquiry.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class InquiryCreateRequest {
    private Long productId;
    private Long sellerId;
    private String title;
    private String content;
}
