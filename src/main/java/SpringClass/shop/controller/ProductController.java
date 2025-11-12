package SpringClass.shop.controller;

import SpringClass.shop.dto.ProductListDTO;
import SpringClass.shop.dto.CreateProductDTO;
import SpringClass.shop.dto.ProductRequest;
import SpringClass.shop.dto.ProductResponse;
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
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@RequestBody CreateProductDTO request) {
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


    @PutMapping
    @Operation(summary = "상품수정", description = "상품수정 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<ProductResponse>> patchProduct(@RequestBody ProductRequest request) {
        ProductResponse result = productService.patchProduct(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "수정되었습니다."));
    }

}
