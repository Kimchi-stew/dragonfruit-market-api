package SpringClass.shop.controller;

import SpringClass.shop.dto.Products.response.AiProductContextResponse;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.AiProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
@Tag(name = "AI Internal API", description = "AI 서버 전용 내부 API")
public class AiProductController {

    private final AiProductService aiProductService;

    @Value("${ai.internal-api-key}")
    private String internalApiKey;

    @GetMapping("/products/{productId}/context")
    @Operation(summary = "AI 상담원용 상품 컨텍스트 조회", description = "AI 서버 전용 내부 API. X-Internal-Api-Key 헤더 필요.")
    public ResponseEntity<ApiResponse<AiProductContextResponse>> getProductContext(
            @PathVariable Long productId,
            @RequestHeader("X-Internal-Api-Key") String apiKey
    ) {
        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail("접근 권한이 없습니다."));
        }

        AiProductContextResponse result = aiProductService.getProductContext(productId);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }
}
