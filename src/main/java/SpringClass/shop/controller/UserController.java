package SpringClass.shop.controller;

import SpringClass.shop.dto.ProductListDTO;
import SpringClass.shop.dto.ProductResponse;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/likes/products")
    @Operation(summary = "내 좋아요한 상품 조회", description = "좋아요한 상품 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<ProductListDTO>>> getLikeProducts() {
        List<ProductListDTO> result = userService.getLikeProducts();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/wish/products")
    @Operation(summary = "내 찜한 상품 조회", description = "찜한 상품 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<ProductListDTO>>> getWishProducts() {
        List<ProductListDTO> result = userService.getWishProducts();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }
}
