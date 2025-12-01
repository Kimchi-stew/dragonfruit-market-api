package SpringClass.shop.controller;

import SpringClass.shop.dto.LikesResponseDTO;
import SpringClass.shop.dto.Reviews.ReviewDeleteDTO;
import SpringClass.shop.dto.Reviews.ReviewListDTO;
import SpringClass.shop.dto.Reviews.ReviewRequest;
import SpringClass.shop.dto.Reviews.ReviewResponseDTO;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("products/{productId}")
    @Operation(summary = "리뷰등록", description = "리뷰 등록 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> createReview
            (@PathVariable Long productId, @RequestBody ReviewRequest request) {
        ReviewResponseDTO result = reviewService.createReview(productId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "등록되었습니다."));
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "상품 리뷰 전체 조회", description = "상품 리뷰 전체 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<ReviewListDTO>>> getReviews
            (@PathVariable Long productId, @RequestParam(required = false) String sort) {
        List<ReviewListDTO> result = reviewService.getReviewLists(productId, sort);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "리뷰 단건 조회", description = "리뷰 단건 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getReview
            (@PathVariable Long reviewId) {
        ReviewResponseDTO result = reviewService.getReview(reviewId);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "리뷰 수정", description = "리뷰 수정 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> patchReview
            (@PathVariable Long reviewId, @RequestBody ReviewRequest request) {
        ReviewResponseDTO result = reviewService.patchReview(reviewId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "수정되었습니다."));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "리뷰 삭제", description = "리뷰 삭제 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<ReviewDeleteDTO>> deleteReview(@PathVariable Long reviewId) {
        ReviewDeleteDTO result = reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.ok(result, "삭제되었습니다."));
    }

    @PostMapping("/likes/{reviewId}")
    @Operation(summary = "리뷰 좋아요", description = "리뷰 좋아요 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<LikesResponseDTO>> likeReview(@PathVariable Long reviewId) {
        LikesResponseDTO result = reviewService.likeReview(reviewId);
        return ResponseEntity.ok(ApiResponse.ok(result, "처리되었습니다."));
    }


}
