package SpringClass.shop.controller;

import SpringClass.shop.dto.*;
import SpringClass.shop.dto.Sellers.*;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/sellers")
public class SellerController {
    private final SellerService sellerService;

    @PostMapping
    @Operation(summary = "상점등록", description = "상점등록 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<SellerResponse>> createSeller(@RequestBody SellerRequest request) {
        SellerResponse result = sellerService.createSeller(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "등록되었습니다."));
    }

    @GetMapping
    @Operation(summary = "전체 상점 조회", description = "전체 상점 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<SellerListDTO>>> getSellers
            (@RequestParam(required = false) String sort) {
        List<SellerListDTO> result = sellerService.getSellers(sort);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/{id}")
    @Operation(summary = "단건 상점 조회", description = "단건 상점 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<SellerResponse>> getSeller(
            @PathVariable Long id
    ) {
        SellerResponse result = sellerService.getSeller(id);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @PutMapping("/{id}")
    @Operation(summary = "상점수정", description = "상점 수정 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<SellerResponse>> patchSeller(@PathVariable Long id, @RequestBody SellerRequest request) {
        SellerResponse result = sellerService.patchSeller(id, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "수정되었습니다."));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "상점삭제", description = "상점 삭제 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<SellerDeleteDTO>> deleteSeller(@PathVariable Long id) {
        SellerDeleteDTO result = sellerService.deleteSeller(id);
        return ResponseEntity.ok(ApiResponse.ok(result, "삭제되었습니다."));
    }

    @PostMapping("/likes/{id}")
    @Operation(summary = "상점좋아요", description = "상점 좋아요 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<LikesResponseDTO>> likeSeller(@PathVariable Long id) {
        LikesResponseDTO result = sellerService.likeSeller(id);
        return ResponseEntity.ok(ApiResponse.ok(result, "처리되었습니다."));
    }

    @PostMapping("/follow/{id}")
    @Operation(summary = "상점팔로우", description = "상점 팔로우 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<SellerFollowDTO>> followSeller(@PathVariable Long id) {
        SellerFollowDTO result = sellerService.followSeller(id);
        return ResponseEntity.ok(ApiResponse.ok(result, "처리되었습니다."));
    }




}
