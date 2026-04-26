package SpringClass.shop.controller;

import SpringClass.shop.dto.Inquiry.request.InquiryAnswerRequest;
import SpringClass.shop.dto.Inquiry.request.InquiryCreateRequest;
import SpringClass.shop.dto.Inquiry.response.InquiryAnswerResponse;
import SpringClass.shop.dto.Inquiry.response.InquiryCreateResponse;
import SpringClass.shop.dto.Inquiry.response.InquiryListResponse;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    @Operation(summary = "문의 등록", description = "문의를 등록하는 API입니다.")
    public ResponseEntity<ApiResponse<InquiryCreateResponse>> createInquiry(@RequestBody InquiryCreateRequest request) {
        InquiryCreateResponse result = inquiryService.createInquiry(request);
        return ResponseEntity.status(201).body(ApiResponse.ok(result, "문의가 등록되었습니다."));
    }

    @GetMapping("/me")
    @Operation(summary = "내 문의 목록 조회", description = "내 문의 목록을 조회하는 API입니다.")
    public ResponseEntity<ApiResponse<Page<InquiryListResponse>>> getMyInquiries(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<InquiryListResponse> result = inquiryService.getMyInquiries(pageable);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @PostMapping("/{id}/answer")
    @Operation(summary = "문의 답변 등록 (판매자/관리자)", description = "문의에 답변을 등록하는 API입니다.")
    public ResponseEntity<ApiResponse<InquiryAnswerResponse>> answerInquiry(
            @PathVariable Long id,
            @RequestBody InquiryAnswerRequest request) {
        InquiryAnswerResponse result = inquiryService.answerInquiry(id, request);
        return ResponseEntity.status(201).body(ApiResponse.ok(result, "답변이 등록되었습니다."));
    }
}
