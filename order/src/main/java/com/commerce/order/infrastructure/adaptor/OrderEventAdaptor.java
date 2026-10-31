package com.commerce.order.infrastructure.adaptor;

import com.commerce.order.core.application.port.out.OrderEventPort;
import com.commerce.order.core.domain.aggregate.Order;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.dto.OrderConfirmedEvent;
import com.commerce.shared.kafka.event.dto.OrderCreatedEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 이벤트 Kafka 어댑터. Order → 이벤트 페이로드 조립 후 TransactionalEventPublisher로 커밋 후 발행한다.
 */
@RequiredArgsConstructor
@Component
public class OrderEventAdaptor implements OrderEventPort {

    private final TransactionalEventPublisher transactionalEventPublisher;

    @Override
    public void publishOrderCreated(Order order, String payMethod, String payProvider, int installment) {
        List<ItemEntry> items = order.getItems().stream()
                .map(oi -> new ItemEntry(oi.getProductId(), oi.getQuantity()))
                .toList();
        String orderId = order.getOrderId().id();

        transactionalEventPublisher.publish(EventTopic.ORDER_CREATED_TOPIC,
                new OrderCreatedEvent(
                        orderId, order.getCustomerId().id(), couponIdOf(order),
                        items, payMethod, payProvider, installment,
                        orderId, LocalDateTime.now()));
    }

    @Override
    public void publishOrderConfirmed(Order order, List<ItemEntry> items,
                                      String payMethod, String payProvider, int installment, String paymentId) {
        String orderId = order.getOrderId().id();

        transactionalEventPublisher.publish(EventTopic.ORDER_CONFIRMED_TOPIC,
                new OrderConfirmedEvent(
                        orderId, order.getCustomerId().id(), couponIdOf(order),
                        items, payMethod, payProvider, installment,
                        order.getOriginAmt().value(), order.getDiscountAmt().value(), order.getResultAmt().value(),
                        paymentId,
                        orderId, LocalDateTime.now()));
    }

    private String couponIdOf(Order order) {
        return order.getCouponId() != null ? order.getCouponId().id() : null;
    }
}
