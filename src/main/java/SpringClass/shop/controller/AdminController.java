package SpringClass.shop.controller;

import SpringClass.shop.dto.Admin.CategoryRequest;
import SpringClass.shop.dto.Admin.CategoryResponse;
import SpringClass.shop.entity.Categories;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    @PostMapping
    @Operation(summary = "상품 카테고리 종류 추가하기", description = "상품 카테고리 종류 추가 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory
            (@RequestBody CategoryRequest request) {
        CategoryResponse result = adminService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "추가되었습니다."));
    }

//    @GetMapping
//    @Operation(summary = "카테고리 전체 조회", description = "카테고리 전체 조회 시 사용하는 API 입니다.")
//    public ResponseEntity<ApiResponse<List<Categories>>> getCategories() {
//        List<Categories> result = adminService.getCategories();
//        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
//    }
}
