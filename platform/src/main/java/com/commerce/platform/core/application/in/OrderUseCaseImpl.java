package com.commerce.platform.core.application.in;

import com.commerce.platform.bootstrap.dto.order.OrderRefundRequest;
import com.commerce.platform.bootstrap.dto.order.OrderRefundResponse;
import com.commerce.platform.bootstrap.dto.order.OrderRequest;
import com.commerce.platform.bootstrap.dto.order.OrderResponse;
import com.commerce.platform.core.application.out.OrderOutputPort;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.vo.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderUseCaseImpl implements OrderUseCase {
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
