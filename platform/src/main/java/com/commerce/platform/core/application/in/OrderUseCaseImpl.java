package com.commerce.platform.core.application.in;

import com.commerce.platform.bootstrap.dto.order.OrderRefundRequest;
import com.commerce.platform.bootstrap.dto.order.OrderRefundResponse;
import com.commerce.platform.bootstrap.dto.order.OrderResponse;
import com.commerce.platform.core.application.out.OrderOutputPort;
import com.commerce.platform.core.application.out.OrderReadOutPort;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.core.domain.dto.ProductView;
import com.commerce.platform.core.domain.enums.OrderStatus;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.core.domain.vo.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class OrderUseCaseImpl implements OrderUseCase {
    private final OrderOutputPort orderOutputPort;
    private final OrderReadOutPort orderReadOutPort;

    @Override
    public OrderId createOrder(Order order) {
        // 주문상품들 정보 세팅
        Map<ProductId, ProductView> productViews = orderReadOutPort.getOrderItemsByProductId(order.getOrderItems());
        order.getOrderItems().stream()
                .forEach(item -> {
                    ProductView pv = productViews.get(item.getProductId());
                    item.setProductInfo(pv.productName(), Money.create(pv.amt()));
                });

        // 쿠폰 조회 및 적용

        // 금액 계산
        orderOutputPort.saveOrder(order);
        return order.getOrderId();
    }

    @Override
    public List<Order> getOrders(CustomerId customerId) {
        return null;
    }

    @Override
    public Order getOrder(OrderId orderId) {
//        return orderOutputPort.findById(orderId)
//                .orElseThrow(() -> new Exception("해당 주문 없음"));
        return null;
    }

    @Override
    public Order cancelOrder(String orderId, String reason) {
        Order order = orderOutputPort.updateOrder(orderId, reason, OrderStatus.CANCELED);
//        return new OrderResponse(order.getOrderId(), order.getStatus().getValue());
        return null;
    }

    @Override
    public Order refundOrder(String orderId, OrderRefundRequest request) {
        Order order = orderOutputPort.updateOrder(orderId, request.reason(), OrderStatus.REFUND);
        // ...
//        return new OrderRefundResponse(orderId, order.getResultAmt().getValue(), order.getStatus().getValue());
        return null;
    }
}
