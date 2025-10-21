package com.commerce.united.port.service;

import com.commerce.united.port.in.OrderUseCase;
import com.commerce.united.port.in.dto.OrderRefundRequest;
import com.commerce.united.port.in.dto.OrderRefundResponse;
import com.commerce.united.port.in.dto.OrderRequest;
import com.commerce.united.port.in.dto.OrderResponse;
import com.commerce.united.port.out.OrderOutputPort;
import com.commerce.united.domain.aggregate.Order;
import com.commerce.united.domain.vo.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderService implements OrderUseCase {
    private final OrderOutputPort orderOutputPort;

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        Order order = orderOutputPort.saveOrder(orderRequest);
        return OrderResponse.from(order);
    }

    @Override
    public OrderResponse getOrder(String orderId) throws Exception {
        return orderOutputPort.findById(orderId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new Exception("해당 주문 없음"));
    }

    @Override
    public OrderResponse cancelOrder(String orderId, String reason) {
        Order order = orderOutputPort.updateOrder(orderId, reason, OrderStatus.CANCELED);
        return new OrderResponse(order.getOrderId(), order.getStatus().getValue());
    }

    @Override
    public OrderRefundResponse refundOrder(String orderId, OrderRefundRequest request) {
        Order order = orderOutputPort.updateOrder(orderId, request.reason(), OrderStatus.REFUND);
        // ...
        return new OrderRefundResponse(orderId, order.getResultAmt().getValue(), order.getStatus().getValue());
    }
}
