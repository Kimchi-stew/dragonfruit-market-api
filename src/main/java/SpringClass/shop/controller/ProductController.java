package SpringClass.shop.controller;

import SpringClass.shop.dto.Admin.response.CategoryResponse;
import SpringClass.shop.dto.Products.request.ProductRequest;
import SpringClass.shop.dto.Products.response.ProductDeleteDTO;
import SpringClass.shop.dto.Products.response.ProductListDTO;
import SpringClass.shop.dto.Products.response.ProductResponse;
import SpringClass.shop.dto.Products.response.WishResponseDTO;
import SpringClass.shop.dto.common.response.LikesResponseDTO;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.PriceSortType;
import SpringClass.shop.enums.ProductCategoryType;
import SpringClass.shop.enums.SortType;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @Operation(summary = "상품등록", description = "상품등록 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@RequestBody ProductRequest request) {
        ProductResponse result = productService.createProduct(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "등록되었습니다."));
    }
    @GetMapping
    @Operation(summary = "전체 상품 조회", description = "전체상품 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<Page<ProductListDTO>>> getProducts
            (@RequestParam(required = false) PriceSortType priceSortType,
             @RequestParam(required = false) ProductCategoryType productCategoryType,
             @RequestParam(required = false) GenderRole genderRole,
             @RequestParam(required = false)SortType sortType,
             Pageable pageable) {
        Page<ProductListDTO> result = productService.getProducts(priceSortType, productCategoryType, genderRole, sortType, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/{id}")
    @Operation(summary = "단건 상품 조회", description = "단건상품 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        ProductResponse result = productService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }


    @PutMapping("/{id}")
    @Operation(summary = "상품수정", description = "상품수정 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<ProductResponse>> patchProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        ProductResponse result = productService.patchProduct(id, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "수정되었습니다."));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "상품삭제", description = "상품삭제 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<ProductDeleteDTO>> deleteProduct(@PathVariable Long id) {
        ProductDeleteDTO result = productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok(result, "삭제되었습니다."));
    }

    @PostMapping("/likes/{id}")
    @Operation(summary = "상품좋아요", description = "상품 좋아요 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<LikesResponseDTO>> likeProduct(@PathVariable Long id) {
        LikesResponseDTO result = productService.likeProduct(id);
        return ResponseEntity.ok(ApiResponse.ok(result, "처리되었습니다."));
    }

    @PostMapping("/wish/{id}")
    @Operation(summary = "상품찜", description = "상품 찜 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<WishResponseDTO>> wishProduct(@PathVariable Long id) {
        WishResponseDTO result = productService.wishProduct(id);
        return ResponseEntity.ok(ApiResponse.ok(result, "처리되었습니다."));
    }

    @GetMapping("/search")
    @Operation(summary = "상품검색", description = "상품 검색 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<ProductListDTO>>> searchProduct
            (@RequestParam(required = false) String keyword) {
        List<ProductListDTO> result = productService.searchProduct(keyword);
        return ResponseEntity.ok(ApiResponse.ok(result, "검색되었습니다."));
    }

    @GetMapping("/category")
    @Operation(summary = "상품 카테고리 전체 조회", description = "카테고리 전체 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> result = productService.getCategories();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }


}
