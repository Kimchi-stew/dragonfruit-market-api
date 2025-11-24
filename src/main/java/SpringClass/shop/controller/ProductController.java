package SpringClass.shop.controller;

import SpringClass.shop.dto.*;
import SpringClass.shop.dto.Products.ProductDeleteDTO;
import SpringClass.shop.dto.Products.ProductListDTO;
import SpringClass.shop.dto.Products.ProductRequest;
import SpringClass.shop.dto.Products.ProductResponse;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/products")
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
    public ResponseEntity<ApiResponse<List<ProductListDTO>>> getProducts
            (@RequestParam(required = false) String category) {
        List<ProductListDTO> result = productService.getProducts(category);
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


}
