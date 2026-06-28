package com.commerce.inventory.bootstrap.event;

import com.commerce.inventory.core.application.port.in.InventoryLedgerUseCase;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.InventoryDeductFailedEvent;
import com.commerce.shared.kafka.event.dto.InventoryDeductedEvent;
import com.commerce.shared.kafka.event.dto.InventoryReserveRollbackEvent;
import com.commerce.shared.kafka.event.dto.InventoryReservedEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * DB 차감 컨슈머(B). DB만 소유한다.
 *
 * inventory.reserved(=Redis 예약 확정) 수신 → DB 원장에 all-or-nothing 차감.
 * - 성공: DB 커밋 後 inventory.deducted 발행(결제 체인 트리거). persistDeduction이 @Transactional이라
 *   이 메서드로 정상 반환된 시점엔 이미 커밋 완료.
 * - 재고 부족(BusinessException(INSUFFICIENT_STOCK), 트랜잭션 롤백됨): inventory.deduct-failed(주문 실패) +
 *   inventory.reserve-rollback(A가 깐 Redis 예약 복원 트리거→C) 발행. 결정적 실패라 재시도하지 않는다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class InventoryDbDeductConsumer {

    private final InventoryLedgerUseCase ledgerUseCase;
    private final TransactionalEventPublisher publisher;

    @KafkaListener(topics = "inventory.reserved", groupId = "inventory-db-deduct")
    public void onReserved(InventoryReservedEvent event) {
        log.debug("[Inventory-DB] inventory.reserved 수신 - orderId: {}", event.orderId());

        try {
            ledgerUseCase.persistDeduction(event.orderId(), event.items());
        } catch (BusinessException e) {
            // persistDeduction의 비즈니스 예외 = 재고 부족(INSUFFICIENT_STOCK). 인프라 예외(DataAccessException 등)는
            // catch되지 않고 전파되어 DefaultErrorHandler 재시도/ DLT 경로를 탄다.
            log.warn("[Inventory-DB] DB 차감 거절 - orderId: {}, code: {}, msg: {}",
                    event.orderId(), e.getCode(), e.getMessage());
            publisher.publish(EventTopic.INVENTORY_DEDUCT_FAILED_TOPIC,
                    new InventoryDeductFailedEvent(event.orderId(), e.getMessage(), event.orderId(), LocalDateTime.now()));
            publisher.publish(EventTopic.INVENTORY_RESERVE_ROLLBACK_TOPIC,
                    new InventoryReserveRollbackEvent(event.orderId(), event.items(), event.orderId(), LocalDateTime.now()));
            return;
        }

        publisher.publish(EventTopic.INVENTORY_DEDUCTED_TOPIC,
                new InventoryDeductedEvent(
                        event.orderId(), event.customerId(), event.couponId(),
                        event.items(), event.payMethod(), event.payProvider(),
                        event.orderId(), LocalDateTime.now()));
        log.info("[Inventory-DB] DB 차감 완료·deducted 발행 - orderId: {}", event.orderId());
    }
}
