package com.commerce.united.controller.web.order;

import com.commerce.united.port.in.OrderUseCase;
import com.commerce.united.port.in.dto.OrderRefundRequest;
import com.commerce.united.port.in.dto.OrderRefundResponse;
import com.commerce.united.port.in.dto.OrderRequest;
import com.commerce.united.port.in.dto.OrderResponse;
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
