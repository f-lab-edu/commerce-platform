package com.commerce.platform.core.application.in;

import com.commerce.platform.bootstrap.dto.order.OrderRefundRequest;
import com.commerce.platform.core.application.in.dto.CreateOrderCommand;
import com.commerce.platform.core.application.in.dto.CreateOrderCommand.OrderItemCommand;
import com.commerce.platform.core.application.in.dto.OrderDetailResponse;
import com.commerce.platform.core.application.in.dto.OrderDetailResponse.OrderItemResponse;
import com.commerce.platform.core.application.in.dto.OrderResponse;
import com.commerce.platform.core.application.out.*;
import com.commerce.platform.core.domain.aggreate.*;
import com.commerce.platform.core.domain.vo.*;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.commerce.shared.exception.BusinessError.*;

@RequiredArgsConstructor
@Service
public class OrderUseCaseImpl implements OrderUseCase {
    private final OrderOutputPort orderOutputPort;
    private final OrderItemOutPort orderItemOutPort;
    private final ProductOutputPort productOutputPort;
    private final CouponOutPort couponOutPort;
    private final CouponIssueOutPort couponIssueOutPort;

    @Override
    public OrderResponse createOrder(CreateOrderCommand orderCommand) {
        Set<ProductId> pidSet = orderCommand.orderItemCommands().stream()
                .map(OrderItemCommand::productId)
                .collect(Collectors.toSet());

        // Order 생성 (주문대기)
        Order order = Order.create(orderCommand.customerId(),
                orderCommand.couponId());

        // OrderItems 생성
        List<OrderItem> orderItems = orderCommand.orderItemCommands().stream()
                .map(item -> {
                    return OrderItem.create(
                            order.getOrderId(),
                            item.productId(),
                            item.quantity());
                })
                .toList();
        orderItemOutPort.saveAll(orderItems);

        // 원금액 계산
        Money totalAmt = calculateTotalAmountFromProducts(orderItems);

        // 쿠폰적용
        Money discountAmt = applyCoupon(order, orderCommand.couponId());

        // 주문완료
        order.confirm(totalAmt, discountAmt);
        orderOutputPort.saveOrder(order);

        return OrderResponse.from(order);
    }

    @Override
    public List<OrderResponse> getOrders(CustomerId customerId) {
        List<Order> orders = orderOutputPort.findByCustomerId(customerId);

        return Optional.ofNullable(orders)
                .orElse(Collections.emptyList())
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Override
    public OrderDetailResponse getOrder(OrderId orderId) {
        Order order = orderOutputPort.findById(orderId)
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));

        List<OrderItem> orderItems = orderItemOutPort.findByOrderId(order.getOrderId());

        List<ProductId> productIds = orderItems.stream()
                .map(oi -> oi.getProductId())
                .toList();

        Map<ProductId, Product> productMap = productOutputPort.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        List<OrderItemResponse> items = orderItems.stream()
                .map(item -> {
                    Product product = productMap.get(item);
                    return new OrderItemResponse(product.getProductId().id(),
                            product.getProductName(),
                            product.getPrice().value(),
                            item.getQuantity().value());
                })
                .toList();

        return new OrderDetailResponse(
                orderId.id(),
                items,
                order.getOriginAmt().value(),
                order.getDiscountAmt().value(),
                order.getResultAmt().value(),
                order.getStatus().getValue(),
                order.getOrderedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd- HH:mm"))
        );
    }

    @Override
    public OrderResponse cancelOrder(OrderId orderId, String reason) {
        Order order = orderOutputPort.findById(orderId)
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));

        order.cancel();
        return OrderResponse.ofCanceled(order);
    }

    @Override
    public OrderResponse refundOrder(OrderId orderId, OrderRefundRequest request) {
        Order order = orderOutputPort.findById(orderId)
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));

        order.refund();
        return OrderResponse.ofCanceled(order);
    }

    /**
     * 쿠폰조회 및 적용
     */
    private Money applyCoupon(Order order, CouponId couponId) {
        if(couponId == null) return Money.of(0);
        CouponIssueId couponIssueId = new CouponIssueId(couponId, order.getCustomerId());

        // 발급된 쿠폰 확인
        CouponIssues issuedCoupon = couponIssueOutPort.findByCouponIssueId(couponIssueId)
                .orElseThrow(() -> new BusinessException(NOT_ISSUED_COUPON));
        issuedCoupon.valid();

        // 쿠폰 정보 조회
        Coupon coupon = couponOutPort.findById(issuedCoupon.getCouponIssueId().couponId())
                .orElseThrow(() -> new BusinessException(INVALID_COUPON));
        Money discountAmt = coupon.calculateDiscountAmt(order.getOriginAmt());

        // 발급쿠폰 적용
        issuedCoupon.use(order.getOrderId());
        couponIssueOutPort.save(issuedCoupon);

        return discountAmt;
    }

    /**
     * 총 주문금액 계산
     */
    private Money calculateTotalAmountFromProducts(List<OrderItem> orderItems) {
        List<ProductId> productIds = orderItems.stream()
                .map(oi -> oi.getProductId())
                .toList();

        Map<ProductId, Product> productMap = productOutputPort.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        return orderItems.stream()
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    product.decreaseStock(item.getQuantity()); // todo 재고 소진 테스트
                    return product.getPrice()
                            .multiply(item.getQuantity());
                })
                .reduce(Money.of(0), Money::add);
    }

}
