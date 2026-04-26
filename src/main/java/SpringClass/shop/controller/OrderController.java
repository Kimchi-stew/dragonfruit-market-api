package SpringClass.shop.controller;

import SpringClass.shop.dto.Orders.request.OrderCreateRequest;
import SpringClass.shop.dto.Orders.request.OrderStatusUpdateRequest;
import SpringClass.shop.dto.Orders.response.OrderCreateResponse;
import SpringClass.shop.dto.Orders.response.OrderDetailResponse;
import SpringClass.shop.dto.Orders.response.OrderListResponse;
import SpringClass.shop.dto.Orders.response.OrderStatusUpdateResponse;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문 생성", description = "결제 버튼 클릭 시 주문을 생성하는 API입니다.")
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(@RequestBody OrderCreateRequest request) {
        OrderCreateResponse result = orderService.createOrder(request);
        return ResponseEntity.status(201).body(ApiResponse.ok(result, "주문이 완료되었습니다."));
    }

    @GetMapping
    @Operation(summary = "내 주문 목록 조회", description = "마이페이지 주문 내역을 조회하는 API입니다.")
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getMyOrders(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<OrderListResponse> result = orderService.getMyOrders(pageable);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/{id}")
    @Operation(summary = "주문 상세 조회", description = "주문 상세 정보를 조회하는 API입니다.")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(@PathVariable Long id) {
        OrderDetailResponse result = orderService.getOrderDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "주문 취소", description = "WAITING 상태의 주문을 취소하는 API입니다.")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("주문이 취소되었습니다."));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "배송 상태 변경 (판매자)", description = "판매자가 배송 상태를 변경하는 API입니다.")
    public ResponseEntity<ApiResponse<OrderStatusUpdateResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateRequest request) {
        OrderStatusUpdateResponse result = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "배송 상태가 변경되었습니다."));
    }
}
