package com.commerce.order.core.application.port.in;

import com.commerce.order.core.application.port.in.dto.CreateOrderCommand;
import com.commerce.order.core.application.port.in.dto.OrderDetailResponse;
import com.commerce.order.core.application.port.in.dto.OrderResponse;
import com.commerce.order.core.application.port.out.OrderOutputPort;
import com.commerce.order.core.domain.aggregate.Order;
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

    @Transactional
    @Override
    public OrderResponse cancelOrder(OrderId orderId, String reason) {
        Order order = orderOutputPort.findById(orderId)
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
        order.cancel();
        return OrderResponse.ofCanceled(order);
    }

    @Transactional
    @Override
    public void orderCompleted(OrderId orderId, Money originAmt, Money discountAmt) {
        Order order = orderOutputPort.findById(orderId)
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
        order.applyAmounts(originAmt, discountAmt);
        order.confirm();
    }
}
