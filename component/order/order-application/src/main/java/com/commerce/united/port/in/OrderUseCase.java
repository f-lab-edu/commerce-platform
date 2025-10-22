package com.commerce.united.port.in;

import com.commerce.united.port.in.dto.OrderRefundRequest;
import com.commerce.united.port.in.dto.OrderRefundResponse;
import com.commerce.united.port.in.dto.OrderRequest;
import com.commerce.united.port.in.dto.OrderResponse;

public interface OrderUseCase {
    OrderResponse createOrder(OrderRequest orderRequest);

    OrderResponse getOrder(String orderId) throws Exception;

    OrderResponse cancelOrder(String orderId, String reason);

    OrderRefundResponse refundOrder(String orderId, OrderRefundRequest request);
}
