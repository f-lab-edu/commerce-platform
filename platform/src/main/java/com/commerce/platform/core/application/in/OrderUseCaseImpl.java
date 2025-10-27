package com.commerce.platform.core.application.in;

import com.commerce.platform.bootstrap.dto.order.OrderRefundRequest;
import com.commerce.platform.core.application.in.dto.CreateOrderCommand;
import com.commerce.platform.core.application.in.dto.CreateOrderCommand.OrderItemCommand;
import com.commerce.platform.core.application.out.CouponOutPort;
import com.commerce.platform.core.application.out.OrderOutputPort;
import com.commerce.platform.core.application.out.ProductOutputPort;
import com.commerce.platform.core.application.out.dto.ProductView;
import com.commerce.platform.core.domain.aggreate.Coupon;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.core.domain.enums.OrderStatus;
import com.commerce.platform.core.domain.vo.*;
import com.commerce.platform.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_ORDER_ID;

@RequiredArgsConstructor
@Service
public class OrderUseCaseImpl implements OrderUseCase {
    private final OrderOutputPort orderOutputPort;
    private final ProductOutputPort productOutputPort;
    private final CouponOutPort couponOutPort;

    @Override
    public Order createOrder(CreateOrderCommand orderCommand) {
        Set<ProductId> pidSet = orderCommand.orderItemCommands().stream()
                .map(OrderItemCommand::productId)
                .collect(Collectors.toSet());

        // 주문상품들 정보 조회
        Map<ProductId, ProductView> productViews = productOutputPort.getOrderItemsByProductId(new ArrayList<>(pidSet));

        // OrderItems 생성
        List<OrderItem> orderItems = orderCommand.orderItemCommands().stream()
                .map(item -> {
                    return OrderItem.create(
                            item.productId(),
                            productViews.get(item.productId()).productName(),
                            Money.create(productViews.get(item.productId()).amt()),
                            item.quantity());
                })
                .toList();

        // Order 생성 (주문대기)
        Order order = Order.create(orderCommand.customerId(),
                orderItems,
                orderCommand.couponId());
        order.calculateAmt();

        // 쿠폰적용
        applyCoupon(order, orderCommand.couponId());

        // 주문완료
        order.orderConfirmed();
        orderOutputPort.saveOrder(order);
        return order;
    }

    @Override
    public List<Order> getOrders(CustomerId customerId) {
        return null;
    }

    @Override
    public Order getOrder(OrderId orderId) {
        return orderOutputPort.findById(orderId)
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
    }

    @Override
    public Order cancelOrder(String orderId, String reason) {
        Order order = orderOutputPort.updateOrder(orderId, reason, OrderStatus.CANCELED);
        // todo ...
        return null;
    }

    @Override
    public Order refundOrder(String orderId, OrderRefundRequest request) {
        Order order = orderOutputPort.updateOrder(orderId, request.reason(), OrderStatus.REFUND);
        // todo ...
        return null;
    }

    /**
     * 쿠폰조회 및 적용
     */
    private void applyCoupon(Order order, CouponId couponId) {
        if(couponId == null) return;

        Coupon coupon = couponOutPort.findById(couponId);
        Money discountAmt = coupon.useCoupon(order.getOriginAmt());

        order.calculateAmtWithCoupon(discountAmt);
    }
}
