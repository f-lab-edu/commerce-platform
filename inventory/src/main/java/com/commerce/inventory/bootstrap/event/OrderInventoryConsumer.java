package com.commerce.inventory.bootstrap.event;

import com.commerce.inventory.bootstrap.dto.InventoryDeductEvent;
import com.commerce.inventory.bootstrap.dto.InventoryRestoreEvent;
import com.commerce.inventory.core.application.port.in.InventoryUseCase;
import com.commerce.inventory.core.domain.vo.StockReserveResult;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.InventoryDeductFailedEvent;
import com.commerce.shared.kafka.event.dto.InventoryDeductedEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 재고 차감/복원 saga 컨슈머 (B1: Redis 단일 원자 Lua).
 *
 * - 차감: 주문 멀티상품을 하나의 원자 연산으로 전부검사→전부차감(all-or-nothing). 멱등(Redis 마커).
 * - 복원: 차감된 주문만 1회 복원(팬텀 가드는 Lua 내부).
 *
 * DB 핫패스 쓰기가 없으므로 @Transactional 불필요. 발행은 TransactionalEventPublisher가
 * 트랜잭션 없을 때 즉시 발행한다.
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
            log.warn("[Inventory] 재고 차감 비즈니스 실패 - orderId: {}, code: {}, msg: {}",
                    event.orderId(), e.getCode(), e.getMessage());
            publishDeductFailed(event, e.getMessage());
            return;
        }

        if (result.isReserved()) {
            transactionalEventPublisher.publish(EventTopic.INVENTORY_DEDUCTED_TOPIC,
                    new InventoryDeductedEvent(
                            event.orderId(), event.customerId(), event.couponId(),
                            event.items(), event.payMethod(), event.payProvider(),
                            event.orderId(), LocalDateTime.now()
                    )
            );
            log.info("[Inventory] 재고 차감 완료 - orderId: {}, status: {}", event.orderId(), result.status());
            return;
        }

        // INSUFFICIENT
        String reason = "재고 부족 - productId: "
                + (result.failedProductId() != null ? result.failedProductId().id() : "unknown");
        log.warn("[Inventory] {} (orderId: {})", reason, event.orderId());
        publishDeductFailed(event, reason);
    }

    @KafkaListener(
            topics = {"order.price-failed", "coupon.apply-failed", "payment.failed", "saga.timeout"},
            groupId = "inventory-service"
    )
    public void handleRestoreInventory(InventoryRestoreEvent event) {
        log.info("[Inventory] 보상 이벤트 수신 - orderId: {}", event.orderId());

        if (event.item() == null) {
            log.info("[Inventory] 복원할 item 없음 - orderId: {}", event.orderId());
            return;
        }

        boolean restored = inventoryUseCase.release(event.orderId(), List.of(event.item()));
        if (restored) {
            log.info("[Inventory] 재고 복원 완료 - orderId: {}", event.orderId());
        } else {
            log.info("[Inventory] 재고 복원 skip(미차감 또는 이미 복원) - orderId: {}", event.orderId());
        }
    }

    private void publishDeductFailed(InventoryDeductEvent event, String reason) {
        transactionalEventPublisher.publish(EventTopic.INVENTORY_DEDUCT_FAILED_TOPIC,
                new InventoryDeductFailedEvent(event.orderId(), reason, event.orderId(), LocalDateTime.now())
        );
    }
}
