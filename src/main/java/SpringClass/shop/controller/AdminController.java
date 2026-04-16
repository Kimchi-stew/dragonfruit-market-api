package SpringClass.shop.controller;

import SpringClass.shop.dto.Admin.CategoryRequest;
import SpringClass.shop.dto.Admin.CategoryResponse;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/category")
    @Operation(summary = "상품 카테고리 종류 추가", description = "상품 카테고리 종류 추가 시 사용하는 API 입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "요청 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "관리자만 카테고리를 추가할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "이미 존재하는 이름 입니다.")
    })
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory
            (@RequestBody CategoryRequest request) {
        CategoryResponse result = adminService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "추가되었습니다."));
    }

    @PutMapping("/category/{id}")
    @Operation(summary = "상품 카테고리 수정", description = "상품 카테고리 수정 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<CategoryResponse>> putCategory
            (@PathVariable Long id, @RequestBody CategoryRequest request) {
        CategoryResponse result = adminService.patchCategory(id, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "수정되었습니다."));
    }

    @DeleteMapping("/category/{id}")
    @Operation(summary = "상품 카테고리 삭제", description = "상품 카테고리 삭제 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<Void>> deleteCategory
            (@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.ok("삭제되었습니다."));
    }


}
