package SpringClass.shop.dto.Inquiry.response;

import SpringClass.shop.entity.Inquiry.InquiryAnswers;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class InquiryAnswerResponse {
    private Long answerId;
    private Long inquiryId;
    private String content;
    private LocalDateTime createdAt;

    public static InquiryAnswerResponse from(InquiryAnswers answer) {
        return InquiryAnswerResponse.builder()
                .answerId(answer.getId())
                .inquiryId(answer.getInquiry().getId())
                .content(answer.getContent())
                .createdAt(answer.getCreatedAt())
                .build();
    }
}
