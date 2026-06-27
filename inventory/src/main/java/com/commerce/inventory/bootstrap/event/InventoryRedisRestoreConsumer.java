package com.commerce.inventory.bootstrap.event;

import com.commerce.inventory.bootstrap.dto.InventoryRestoreEvent;
import com.commerce.inventory.core.application.port.in.InventoryUseCase;
import com.commerce.shared.kafka.event.dto.InventoryReserveRollbackEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Redis 복원 컨슈머(C). Redis만 소유한다.
 *
 * - 다운스트림 보상(order.price-failed / payment.failed): 차감이 확정된(=inventory.deducted 발행된) 주문이므로 복원.
 * - reserve-rollback(B의 DB 차감 거절): A가 깐 Redis 예약을 주문 전체 items로 되돌린다.
 *
 * 두 경로 모두 "Redis 차감이 확정된 흐름"에서만 도달하므로 팬텀(미차감 복원) 위험이 없다.
 * DB는 만지지 않는다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class InventoryRedisRestoreConsumer {

    private final InventoryUseCase inventoryUseCase;

    @KafkaListener(topics = {"order.price-failed", "coupon.apply-failed", "payment.failed", "saga.timeout"}, groupId = "inventory-redis-restore")
    public void onCompensation(InventoryRestoreEvent event) {
        if (event.item() == null) {
            log.info("[Inventory-RedisRestore] 복원할 item 없음 - orderId: {}", event.orderId());
            return;
        }
        inventoryUseCase.release(event.orderId(), List.of(event.item()));
        log.info("[Inventory-RedisRestore] 보상 복원 - orderId: {}", event.orderId());
    }

    @KafkaListener(topics = "inventory.reserve-rollback", groupId = "inventory-redis-restore")
    public void onReserveRollback(InventoryReserveRollbackEvent event) {
        if (event.items() == null || event.items().isEmpty()) {
            log.info("[Inventory-RedisRestore] rollback할 items 없음 - orderId: {}", event.orderId());
            return;
        }
        inventoryUseCase.release(event.orderId(), event.items());
        log.info("[Inventory-RedisRestore] 예약 rollback 복원 - orderId: {}", event.orderId());
    }
}
