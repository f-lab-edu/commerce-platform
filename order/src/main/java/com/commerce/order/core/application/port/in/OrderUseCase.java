package com.commerce.order.core.application.port.in;

import com.commerce.order.bootstrap.dto.OrderRefundRequest;
import com.commerce.order.core.application.port.in.dto.CreateOrderCommand;
import com.commerce.order.core.application.port.in.dto.OrderDetailResponse;
import com.commerce.order.core.application.port.in.dto.OrderResponse;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.OrderId;

import java.util.List;

public interface OrderUseCase {
    OrderResponse createOrder(CreateOrderCommand orderCommand);
    List<OrderResponse> getOrders(CustomerId customerId);
    OrderDetailResponse getOrder(OrderId orderId);
    OrderResponse cancelOrder(OrderId orderId, String reason);
    OrderResponse refundOrder(OrderId orderId, OrderRefundRequest request);
}
