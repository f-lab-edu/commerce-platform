package com.commerce.inventory.core.application.port.in;

import com.commerce.inventory.core.domain.aggregate.Inventory;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;


/**
 * 재고 관리 유스케이스
 */
public interface InventoryUseCase {
    void decreaseStock(ProductId productId, Quantity quantity);
    void increaseStock(ProductId productId, Quantity quantity);
    void setStock(ProductId productId, Quantity quantity);
    Inventory getStock(ProductId productId);
}
