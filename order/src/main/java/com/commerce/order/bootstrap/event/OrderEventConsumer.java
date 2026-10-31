package com.commerce.order.bootstrap.event;

import com.commerce.order.bootstrap.event.dto.OrderCompletedEvent;
import com.commerce.order.bootstrap.event.dto.OrderFailedEvent;
import com.commerce.order.core.application.port.in.OrderUseCase;
import com.commerce.order.core.application.port.in.dto.CompleteOrderCommand;
import com.commerce.order.core.domain.vo.ItemSpec;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;

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
            orderUseCase.orderCompleted(new CompleteOrderCommand(
                    OrderId.of(event.orderId()),
                    Money.of(event.originAmt()),
                    Money.of(event.discountAmt()),
                    event.paymentId(),
                    toItemSpecs(event.items()),
                    event.payMethod(),
                    event.payProvider(),
                    event.installment()
            ));
        } catch (BusinessException e) {
            // 결제는 승인됐는데 주문 확정 실패(이미 취소된 주문 등) → 수동 환불 대상
            log.error("handleOrderCompleted 확정 실패(환불 필요) - orderId: {}, paymentId: {}, approvedAmt: {}, code: {}, msg: {}",
                    event.orderId(), event.paymentId(), event.approvedAmt(), e.getCode(), e.getMessage());
        }
    }

    private List<ItemSpec> toItemSpecs(List<ItemEntry> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(item -> new ItemSpec(item.productId(), item.quantity()))
                .toList();
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
