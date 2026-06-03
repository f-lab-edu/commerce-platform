package com.commerce.order.core.application.port.in;

import com.commerce.order.bootstrap.dto.OrderRefundRequest;
import com.commerce.order.core.application.port.in.dto.CreateOrderCommand;
import com.commerce.order.core.application.port.in.dto.OrderDetailResponse;
import com.commerce.order.core.application.port.in.dto.OrderResponse;
import com.commerce.order.core.application.port.out.OrderOutputPort;
import com.commerce.order.core.domain.aggregate.Order;
import com.commerce.order.core.domain.enums.OrderStatus;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.commerce.shared.exception.BusinessError.INVALID_ORDER_ID;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderUseCaseImpl implements OrderUseCase {
    private final OrderOutputPort orderOutputPort;
    private final TransactionalEventPublisher transactionalEventPublisher;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderCommand orderCommand) {
        List<Order.ItemSpec> itemSpecs = orderCommand.orderItemCommands().stream()
                .map(item -> new Order.ItemSpec(item.productId(), item.quantity()))
                .toList();

        Order order = Order.create(orderCommand.customerId(), orderCommand.couponId(), itemSpecs);
        orderOutputPort.saveOrder(order);

        transactionalEventPublisher.publish(
                EventTopic.ORDER_CREATED_TOPIC,
                order.toCreatedEvent(orderCommand.payMethod(), orderCommand.payProvider())
        );

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

        // todo: Task 4 이후 상품 정보는 이벤트 기반으로 대체 예정
        List<OrderDetailResponse.OrderItemResponse> items = List.of();

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
    @Transactional
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

    @Transactional
    @Override
    public void orderCompleted(OrderId orderId, Money originAmt, Money discountAmt) {
        Order order = orderOutputPort.findById(orderId).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            return; // 이미 처리됨
        }

        order.applyAmounts(originAmt, discountAmt);
        order.confirm();
        order.changeStatusAfterPay(true);
        orderOutputPort.saveOrder(order);
    }

    @Transactional
    @Override
    public void orderRejected(OrderId orderId) {
        Order order = orderOutputPort.findById(orderId).orElse(null);
        if (order.getStatus() != OrderStatus.PENDING) {
            return; // 멱등: 이미 처리됨
        }

        order.cancel();
        orderOutputPort.saveOrder(order);
    }
}
