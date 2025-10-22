package com.commerce.platform.core.application.in;

import com.commerce.platform.bootstrap.dto.order.OrderRefundRequest;
import com.commerce.platform.bootstrap.dto.order.OrderRefundResponse;
import com.commerce.platform.bootstrap.dto.order.OrderRequest;
import com.commerce.platform.bootstrap.dto.order.OrderResponse;

public interface OrderUseCase {
    OrderResponse createOrder(OrderRequest orderRequest);
    OrderResponse getOrder(String orderId) throws Exception;
    OrderResponse cancelOrder(String orderId, String reason);
    OrderRefundResponse refundOrder(String orderId, OrderRefundRequest request);
}
