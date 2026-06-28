package com.commerce.inventory.bootstrap.event;

import com.commerce.inventory.bootstrap.dto.InventoryDeductEvent;
import com.commerce.inventory.core.application.port.in.InventoryUseCase;
import com.commerce.inventory.core.domain.vo.StockReserveResult;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.InventoryDeductFailedEvent;
import com.commerce.shared.kafka.event.dto.InventoryReservedEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Redis 예약 컨슈머(A). Redis만 소유한다.
 *
 * order.created 수신 → Redis 원자 예약(deduct.lua). 성공 시 inventory.reserved 발행(→ DB 차감 컨슈머 B).
 * Redis 재고 부족/예외 시 inventory.deduct-failed 발행(→ order 주문 실패). 깐 게 없으므로 복원 트리거는 보내지 않는다.
 * DB·보상은 만지지 않는다(보상은 C/D 담당).
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderInventoryConsumer {

    private final InventoryUseCase inventoryUseCase;
    private final TransactionalEventPublisher transactionalEventPublisher;

    @KafkaListener(topics = "order.created", groupId = "inventory-service")
    public void handleDeductInventory(InventoryDeductEvent event) {
        log.info("[Inventory] order.created 수신 - orderId: {}", event.orderId());

        StockReserveResult result;
        try {
            result = inventoryUseCase.reserve(event.orderId(), event.items());
        } catch (BusinessException e) {
            log.warn("[Inventory] Redis 예약 비즈니스 실패 - orderId: {}, code: {}, msg: {}",
                    event.orderId(), e.getCode(), e.getMessage());
            publishDeductFailed(event, e.getMessage());
            return;
        }

        if (result.isReserved()) {
            transactionalEventPublisher.publish(EventTopic.INVENTORY_RESERVED_TOPIC,
                    new InventoryReservedEvent(
                            event.orderId(), event.customerId(), event.couponId(),
                            event.items(), event.payMethod(), event.payProvider(),
                            event.orderId(), LocalDateTime.now()));
            log.info("[Inventory] Redis 예약 완료 - orderId: {}, status: {}", event.orderId(), result.status());
            return;
        }

        // INSUFFICIENT
        String reason = "재고 부족 - productId: "
                + (result.failedProductId() != null ? result.failedProductId().id() : "unknown");
        log.warn("[Inventory] {} (orderId: {})", reason, event.orderId());
        publishDeductFailed(event, reason);
    }

    private void publishDeductFailed(InventoryDeductEvent event, String reason) {
        transactionalEventPublisher.publish(EventTopic.INVENTORY_DEDUCT_FAILED_TOPIC,
                new InventoryDeductFailedEvent(event.orderId(), reason, event.orderId(), LocalDateTime.now()));
    }
}
