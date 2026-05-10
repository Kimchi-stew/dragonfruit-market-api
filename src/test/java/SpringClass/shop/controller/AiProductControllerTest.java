package SpringClass.shop.controller;

import SpringClass.shop.dto.Products.response.AiProductContextResponse;
import SpringClass.shop.exceptions.product.ProductNotFoundException;
import SpringClass.shop.global.GlobalApiResponseHandler;
import SpringClass.shop.service.AiProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AiProductControllerTest {

    @Mock private AiProductService aiProductService;
    @InjectMocks private AiProductController aiProductController;

    private MockMvc mockMvc;
    private static final String VALID_KEY = "test-internal-key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiProductController, "internalApiKey", VALID_KEY);
        mockMvc = MockMvcBuilders.standaloneSetup(aiProductController)
                .setControllerAdvice(new GlobalApiResponseHandler())
                .build();
    }

    // ── GET /ai/products/{productId}/context ──────────────────────────

    @Test
    @DisplayName("올바른 내부 API 키로 상품 컨텍스트 조회 시 200 반환")
    void getProductContext_validKey_returns200() throws Exception {
        AiProductContextResponse response = AiProductContextResponse.builder()
                .productId(1L)
                .name("테스트 상품")
                .price(new BigDecimal("29900"))
                .stock(10)
                .description("상품 설명")
                .sellerName("테스트샵")
                .categories(List.of("의류"))
                .images(List.of("https://example.com/img.jpg"))
                .build();

        when(aiProductService.getProductContext(1L)).thenReturn(response);

        mockMvc.perform(get("/ai/products/1/context")
                        .header("X-Internal-Api-Key", VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(1))
                .andExpect(jsonPath("$.data.name").value("테스트 상품"))
                .andExpect(jsonPath("$.data.price").value(29900))
                .andExpect(jsonPath("$.data.stock").value(10))
                .andExpect(jsonPath("$.data.sellerName").value("테스트샵"))
                .andExpect(jsonPath("$.data.categories[0]").value("의류"))
                .andExpect(jsonPath("$.data.images[0]").value("https://example.com/img.jpg"));
    }

    @Test
    @DisplayName("잘못된 API 키로 요청 시 403 반환")
    void getProductContext_invalidKey_returns403() throws Exception {
        mockMvc.perform(get("/ai/products/1/context")
                        .header("X-Internal-Api-Key", "wrong-key"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        verify(aiProductService, never()).getProductContext(any());
    }

    @Test
    @DisplayName("API 키 헤더 누락 시 400 반환")
    void getProductContext_missingKey_returns400() throws Exception {
        mockMvc.perform(get("/ai/products/1/context"))
                .andExpect(status().isBadRequest());

        verify(aiProductService, never()).getProductContext(any());
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID 조회 시 404 반환")
    void getProductContext_productNotFound_returns404() throws Exception {
        when(aiProductService.getProductContext(999L))
                .thenThrow(new ProductNotFoundException("상품을 찾을 수 없습니다."));

        mockMvc.perform(get("/ai/products/999/context")
                        .header("X-Internal-Api-Key", VALID_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
