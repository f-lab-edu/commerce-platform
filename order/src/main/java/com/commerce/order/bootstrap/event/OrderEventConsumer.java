package com.commerce.order.bootstrap.event;

import com.commerce.order.bootstrap.event.dto.OrderCompletedEvent;
import com.commerce.order.bootstrap.event.dto.OrderFailedEvent;
import com.commerce.order.core.application.port.in.OrderUseCase;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 주문 saga 이벤트 컨슈머.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventConsumer {

    private final OrderUseCase orderUseCase;

    @KafkaListener(topics = "payment.completed", groupId = "order-service")
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("[Order] payment.completed 수신 - orderId: {}", event.orderId());

        try {
            orderUseCase.orderCompleted(
                    OrderId.of(event.orderId()),
                    Money.of(event.originAmt()),
                    Money.of(event.discountAmt())
            );
        } catch (BusinessException e) {
            log.warn("handleOrderCompleted - orderId: {}, code: {}, msg: {}",
                    event.orderId(), e.getCode(), e.getMessage());
        }
    }

    @KafkaListener(
            topics = {"inventory.deduct-failed", "order.price-failed", "coupon.apply-failed", "payment.failed"},
            groupId = "order-service"
    )
    public void handleOrderFailed(OrderFailedEvent event,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("[Order] saga 실패 이벤트 수신 - topic: {}, orderId: {}", topic, event.orderId());

        try {
            orderUseCase.cancelOrder(OrderId.of(event.orderId()), "주문실패:" + topic);
        } catch (BusinessException e) {
            log.warn("handleOrderFailed - orderId: {}, code: {}, msg: {}",
                    event.orderId(), e.getCode(), e.getMessage());
        }
    }
}
