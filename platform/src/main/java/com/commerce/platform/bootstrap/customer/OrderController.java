package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.order.OrderRefundRequest;
import com.commerce.platform.bootstrap.dto.order.OrderRefundResponse;
import com.commerce.platform.bootstrap.dto.order.OrderRequest;
import com.commerce.platform.bootstrap.dto.order.OrderResponse;
import com.commerce.platform.core.application.in.OrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/order")
@RestController
public class OrderController {
    private final OrderUseCase orderUseCase;

    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderRequest orderRequest) {
        return orderUseCase.createOrder(orderRequest);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable String orderId) {
        try {
            return orderUseCase.getOrder(orderId);
        } catch (Exception e) {
            return null;
        }
    }

    @PatchMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(@PathVariable String orderId,
                                     @RequestBody String reson) {
        return orderUseCase.cancelOrder(orderId, reson);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderRefundResponse refundOrder(@PathVariable String orderId,
                                           @RequestBody OrderRefundRequest request) {
        return orderUseCase.refundOrder(orderId, request);
    }
}