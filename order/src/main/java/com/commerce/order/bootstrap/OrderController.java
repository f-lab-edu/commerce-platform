package com.commerce.order.bootstrap;

import com.commerce.order.bootstrap.dto.OrderRequest;
import com.commerce.order.core.application.port.in.OrderUseCase;
import com.commerce.order.core.application.port.in.dto.CreateOrderCommand;
import com.commerce.order.core.application.port.in.dto.OrderDetailResponse;
import com.commerce.order.core.application.port.in.dto.OrderResponse;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.OrderId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/orders")
@RestController
public class OrderController {
    private final OrderUseCase orderUseCase;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
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
                                     @RequestBody String reason) {
        OrderResponse canceledOrder = orderUseCase.cancelOrder(OrderId.of(orderId), reason);
        return ResponseEntity.ok(canceledOrder);
    }
}