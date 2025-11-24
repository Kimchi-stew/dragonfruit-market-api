package SpringClass.shop.controller;

import SpringClass.shop.dto.Cart.CartRequest;
import SpringClass.shop.dto.Cart.CartResponse;
import SpringClass.shop.dto.Products.ProductSummaryDTO;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/cart")
public class CartController {
    private CartService cartService;

    @PostMapping
    @Operation(summary = "장바구니에 상품 등록", description = "장바구니 상품 등록 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<ProductSummaryDTO>> plusProduct
            (@RequestBody CartRequest request) {
        ProductSummaryDTO result = cartService.plusProduct(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "상품이 추가되었습니다."));
    }

    @GetMapping
    @Operation(summary = "장바구니 조회", description = "장바구니 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        CartResponse result = cartService.getCart();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @DeleteMapping
    @Operation(summary = "장바구니 상품 삭제", description = "장바구니 상품 삭제 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<Void>> deleteProduct
            (@RequestBody CartRequest request) {
        cartService.deleteProduct(request);
        return ResponseEntity.ok(ApiResponse.ok("삭제되었습니다."));
    }

}
