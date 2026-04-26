package SpringClass.shop.dto.Inquiry.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class InquiryAnswerRequest {
    private String content;
}
