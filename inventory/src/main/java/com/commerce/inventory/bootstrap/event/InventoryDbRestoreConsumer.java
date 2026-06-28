package com.commerce.inventory.bootstrap.event;

import com.commerce.inventory.bootstrap.dto.InventoryRestoreEvent;
import com.commerce.inventory.core.application.port.in.InventoryLedgerUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DB 복원 컨슈머(D). DB만 소유한다.
 *
 * 다운스트림 보상(order.price-failed / payment.failed)만 구독한다 — 이 시점엔 DB 차감이 확정(inventory.deducted)된 후다.
 * B의 reserve-rollback은 구독하지 않는다: B 실패 시 DB 트랜잭션은 롤백되어 복원할 대상이 없기 때문.
 * 멱등성·차감선행 팬텀 가드는 persistRestoration 내부(ProcessedEvent)에 있다. Redis는 만지지 않는다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class InventoryDbRestoreConsumer {

    private final InventoryLedgerUseCase ledgerUseCase;

    @KafkaListener(topics = {"order.price-failed", "coupon.apply-failed", "payment.failed", "saga.timeout"}, groupId = "inventory-db-restore")
    public void onCompensation(InventoryRestoreEvent event) {
        if (event.item() == null) {
            log.info("[Inventory-DbRestore] 복원할 item 없음 - orderId: {}", event.orderId());
            return;
        }
        ledgerUseCase.persistRestoration(event.orderId(), List.of(event.item()));
        log.info("[Inventory-DbRestore] DB 원장 복원 - orderId: {}", event.orderId());
    }
}
