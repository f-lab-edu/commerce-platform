package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.order.*;
import com.commerce.platform.core.application.in.OrderUseCase;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.core.domain.vo.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/orders")
@RestController
public class OrderController {
    private final OrderUseCase orderUseCase;

    // todo 장바구니에서 여러상품 주문

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        Order order = OrderRequest.to(orderRequest);
        orderUseCase.createOrder(order);

        return ResponseEntity.ok(
                OrderResponse.from(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam String customerId) {
        List<Order> orders = orderUseCase.getOrders(CustomerId.of(customerId));

        return ResponseEntity.ok(
                orders.stream()
                        .map(OrderResponse::from)
                        .toList()
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable String orderId) {
        Order order = orderUseCase.getOrder(OrderId.of(orderId));
        return ResponseEntity.ok(
                OrderDetailResponse.from(order)
        );
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId,
                                     @RequestBody String reson) {
        Order canceledOrder = orderUseCase.cancelOrder(orderId, reson);
        return ResponseEntity.ok(
                OrderResponse.ofCanceled(canceledOrder)
        );
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<OrderRefundResponse> refundOrder(@PathVariable String orderId,
                                           @RequestBody OrderRefundRequest request) {
        Order refundedOrder = orderUseCase.refundOrder(orderId, request);
        return ResponseEntity.ok(
                OrderRefundResponse.from(refundedOrder)
        );
    }
}