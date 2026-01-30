package com.commerce.inventory.application.port.in;

import com.commerce.inventory.application.port.in.event.OrderCompletedEvent;

/**
 * 재고 관리 유스케이스
 */
public interface InventoryUseCase {
    /**
     * 주문 완료 이벤트 처리
     */
    void handleOrderCompleted(OrderCompletedEvent event);
}
