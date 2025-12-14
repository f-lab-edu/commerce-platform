package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.order.OrderRefundRequest;
import com.commerce.platform.bootstrap.dto.order.OrderRequest;
import com.commerce.platform.core.application.port.in.OrderUseCase;
import com.commerce.platform.core.application.port.in.dto.CreateOrderCommand;
import com.commerce.platform.core.application.port.in.dto.OrderDetailResponse;
import com.commerce.platform.core.application.port.in.dto.OrderResponse;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/orders")
@RestController
public class OrderController {
    private final OrderUseCase orderUseCase;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        CreateOrderCommand orderCommand = CreateOrderCommand.from(orderRequest);
        OrderResponse order = orderUseCase.createOrder(orderCommand);

        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam String customerId) {
        List<OrderResponse> orders = orderUseCase.getOrders(CustomerId.of(customerId));

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable String orderId) {
        OrderDetailResponse order = orderUseCase.getOrder(OrderId.of(orderId));
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId,
                                     @RequestBody String reson) {
        OrderResponse canceledOrder = orderUseCase.cancelOrder(OrderId.of(orderId), reson);
        return ResponseEntity.ok(canceledOrder);
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<OrderResponse> refundOrder(@PathVariable String orderId,
                                           @RequestBody OrderRefundRequest request) {
        OrderResponse refundedOrder = orderUseCase.refundOrder(OrderId.of(orderId), request);
        return ResponseEntity.ok(refundedOrder);
    }
}