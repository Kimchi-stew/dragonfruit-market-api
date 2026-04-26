package SpringClass.shop.dto.Inquiry.response;

import SpringClass.shop.entity.Inquiry.Inquiries;
import SpringClass.shop.enums.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class InquiryCreateResponse {
    private Long inquiryId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private InquiryStatus status;

    public static InquiryCreateResponse from(Inquiries inquiry) {
        return InquiryCreateResponse.builder()
                .inquiryId(inquiry.getId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .createdAt(inquiry.getCreatedAt())
                .status(inquiry.getStatus())
                .build();
    }
}
