package com.commerce.platform.core.application.in;

import com.commerce.platform.bootstrap.dto.order.OrderRefundRequest;
import com.commerce.platform.core.application.in.dto.CreateOrderCommand;
import com.commerce.platform.core.application.in.dto.OrderDetailResponse;
import com.commerce.platform.core.application.in.dto.OrderResponse;
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
