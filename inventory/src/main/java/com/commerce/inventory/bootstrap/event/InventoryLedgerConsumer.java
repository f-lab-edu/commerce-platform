package com.commerce.inventory.bootstrap.event;

import com.commerce.inventory.core.application.port.in.InventoryLedgerUseCase;
import com.commerce.shared.kafka.event.dto.InventoryDeductedEvent;
import com.commerce.shared.kafka.event.dto.InventoryRestoredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 비동기 durable 원장 컨슈머 (adapter).
 *
 * Redis 동기 게이트가 확정한 차감/복원 결과를, DB 재고 행에 비동기로 영속화하도록 위임한다.
 * Redis가 단일 진실원천(라이브 카운터)이고 DB는 이를 뒤따르는 durable 원장이다 — 그래서 재시작
 * warmup이 stale 원본이 아니라 실제 잔량을 가진 DB에서 안전하게 재시드할 수 있다.
 *
 * 멱등성·트랜잭션 경계는 {@link InventoryLedgerUseCase} 구현에 있다.
 * group=inventory-ledger 로, product 등 다른 inventory.deducted 소비자와 독립적으로 소비한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class InventoryLedgerConsumer {

    private final InventoryLedgerUseCase ledgerUseCase;

    @KafkaListener(topics = "inventory.deducted", groupId = "inventory-ledger")
    public void onDeducted(InventoryDeductedEvent event) {
        log.debug("[Inventory-Ledger] inventory.deducted 수신 - orderId: {}", event.orderId());
        ledgerUseCase.persistDeduction(event.orderId(), event.items());
    }

    @KafkaListener(topics = "inventory.restored", groupId = "inventory-ledger")
    public void onRestored(InventoryRestoredEvent event) {
        log.debug("[Inventory-Ledger] inventory.restored 수신 - orderId: {}", event.orderId());
        ledgerUseCase.persistRestoration(event.orderId(), event.items());
    }
}
