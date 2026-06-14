package com.commerce.inventory.core.application.port.in;

import com.commerce.shared.kafka.event.dto.StockCommandEvent;

/**
 * B2 상품 단위 재고 커맨드 인바운드 포트. type(DEDUCT/REPLENISH)으로 분기해 DB 단일 조건부 UPDATE로 처리한다.
 */
public interface InventoryStockCommandUseCase {

    void handle(StockCommandEvent event);
}
