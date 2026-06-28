package com.commerce.inventory.core.application.port.in;

import com.commerce.inventory.core.domain.aggregate.Inventory;
import com.commerce.inventory.core.domain.vo.StockReserveResult;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;

import java.util.List;


/**
 * 재고 관리 유스케이스
 */
public interface InventoryUseCase {

    /** 주문 멀티상품 재고를 원자적으로 차감(예약)한다. all-or-nothing. 멱등성 미보장(추후 outbox). */
    StockReserveResult reserve(String orderId, List<ItemEntry> items);

    /** 차감된 주문 재고를 복원(보상)한다. 복원 1회. 실제 복원 시 true. */
    boolean release(String orderId, List<ItemEntry> items);

    void decreaseStock(ProductId productId, Quantity quantity);
    void increaseStock(ProductId productId, Quantity quantity);
    void setStock(ProductId productId, Quantity quantity);
    Inventory getStock(ProductId productId);
}
