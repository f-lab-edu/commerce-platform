package com.commerce.inventory.core.application.port.in;

import com.commerce.shared.kafka.event.dto.ItemEntry;

import java.util.List;

/**
 * 비동기 durable 원장 인바운드 포트.
 * Redis 동기 게이트가 확정한 차감/복원 결과를 DB 재고 원장에 멱등적으로 영속화한다.
 */
public interface InventoryLedgerUseCase {

    /** 차감을 DB 원장에 반영(멱등). */
    void persistDeduction(String orderId, List<ItemEntry> items);

    /** 복원을 DB 원장에 반영(멱등 + 차감 선행 팬텀 가드). */
    void persistRestoration(String orderId, List<ItemEntry> items);
}
