package com.commerce.inventory.core.application.port.in;

import com.commerce.inventory.core.application.port.out.InventoryStockPort;
import com.commerce.shared.exception.BusinessError;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 비동기 durable 원장 적용. Redis가 단일 진실원천(동기 게이트)이고 DB는 이를 뒤따르는 durable 원장이다.
 **/
@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryLedgerUseCaseImpl implements InventoryLedgerUseCase {

    private final InventoryStockPort stockPort;

    @Override
    @Transactional
    public void persistDeduction(String orderId, List<ItemEntry> items) {
        for (ItemEntry item : items) {
            int affected = stockPort.deductIfEnough(item.productId(), item.quantity().value());
            if (affected == 0) {
                // DB가 진실원천: 재고 부족 = 오버셀 최종 거절. BusinessException으로 트랜잭션을 롤백해
                // 같은 주문에서 이미 깐 항목들까지 전부 원복한다(all-or-nothing).
                // BusinessException이라 DefaultErrorHandler가 재시도하지 않는다(재고 부족은 재시도 무의미).
                log.warn("[Inventory-Ledger] DB 재고 부족(차감 거절) - orderId: {}, productId: {}, qty: {}",
                        orderId, item.productId().id(), item.quantity().value());
                throw new BusinessException(BusinessError.INSUFFICIENT_STOCK);
            }
        }

        log.info("[Inventory-Ledger] 차감 영속화 완료 - orderId: {}, items: {}", orderId, items.size());
    }

    @Override
    @Transactional
    public void persistRestoration(String orderId, List<ItemEntry> items) {
        for (ItemEntry item : items) {
            stockPort.replenish(item.productId(), item.quantity().value());
        }

        log.info("[Inventory-Ledger] 복원 영속화 완료 - orderId: {}, items: {}", orderId, items.size());
    }
}
