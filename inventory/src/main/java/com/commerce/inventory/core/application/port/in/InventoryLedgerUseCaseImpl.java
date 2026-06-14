package com.commerce.inventory.core.application.port.in;

import com.commerce.inventory.core.application.port.out.InventoryStockPort;
import com.commerce.inventory.core.application.port.out.ProcessedEventPort;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.ProductId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 비동기 durable 원장 적용. Redis가 단일 진실원천(동기 게이트)이고 DB는 이를 뒤따르는 durable 원장이다.
 *
 * 멱등성(eventId = orderId + ":LEDGER-DEDUCT" / ":LEDGER-RESTORE")과 DB 갱신을 같은 @Transactional로
 * 원자 커밋해 Kafka redelivery 시 이중 반영을 막는다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryLedgerUseCaseImpl implements InventoryLedgerUseCase {

    private static final String DEDUCT_SUFFIX = ":LEDGER-DEDUCT";
    private static final String RESTORE_SUFFIX = ":LEDGER-RESTORE";

    private final InventoryStockPort stockPort;
    private final ProcessedEventPort processedEventPort;

    @Override
    @Transactional
    public void persistDeduction(String orderId, List<ItemEntry> items) {
        String eventId = orderId + DEDUCT_SUFFIX;
        if (processedEventPort.exists(eventId)) {
            log.debug("[Inventory-Ledger] 이미 영속화된 차감 skip - eventId: {}", eventId);
            return;
        }

        for (ItemEntry item : items) {
            ProductId productId = item.productId();
            int affected = stockPort.deductIfEnough(productId, item.quantity().value());
            if (affected == 0) {
                // Redis 게이트가 승인한 차감을 DB 원장이 따라가지 못함 = 드리프트(원장이 Redis보다 과소).
                // 재시도해도 동일하므로 throw하지 않고 ERROR 알람 후 진행한다(수동 정합화 대상).
                log.error("[Inventory-Ledger] 원장 차감 실패(드리프트) - orderId: {}, productId: {}, qty: {}",
                        orderId, productId.id(), item.quantity().value());
            }
        }

        processedEventPort.markProcessed(eventId);
        log.info("[Inventory-Ledger] 차감 영속화 완료 - orderId: {}, items: {}", orderId, items.size());
    }

    @Override
    @Transactional
    public void persistRestoration(String orderId, List<ItemEntry> items) {
        String restoreId = orderId + RESTORE_SUFFIX;
        if (processedEventPort.exists(restoreId)) {
            log.debug("[Inventory-Ledger] 이미 영속화된 복원 skip - eventId: {}", restoreId);
            return;
        }

        // 팬텀 가드: DB 원장에 차감이 반영된 주문만 복원한다. 차감보다 복원이 먼저 도착한
        // (드문) 재정렬 시엔 복원을 건너뛰어 DB가 과소(절대 과다 아님)로 남는다 = 오버셀 방지 우선.
        if (!processedEventPort.exists(orderId + DEDUCT_SUFFIX)) {
            log.warn("[Inventory-Ledger] 원장 차감 기록 없음 - 복원 skip(팬텀 가드) - orderId: {}", orderId);
            return;
        }

        for (ItemEntry item : items) {
            stockPort.replenish(item.productId(), item.quantity().value());
        }

        processedEventPort.markProcessed(restoreId);
        log.info("[Inventory-Ledger] 복원 영속화 완료 - orderId: {}, items: {}", orderId, items.size());
    }
}
