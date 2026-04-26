package SpringClass.shop.dto.Inquiry.response;

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
public class InquiryListResponse {
    private Long inquiryId;
    private String title;
    private InquiryStatus status;
    private LocalDateTime createdAt;
    private AnswerSummary answer;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AnswerSummary {
        private String content;
        private LocalDateTime createdAt;
    }
}
