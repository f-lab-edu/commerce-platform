package com.commerce.inventory.core.application.port.in;

import com.commerce.inventory.core.application.port.out.InventoryOutport;
import com.commerce.inventory.core.application.port.out.InventoryStockCache;
import com.commerce.inventory.core.domain.aggregate.Inventory;
import com.commerce.inventory.core.domain.vo.StockReserveResult;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.commerce.shared.exception.BusinessError.STOCK_NOT_AVAILABLE;

@RequiredArgsConstructor
@Service
public class InventoryUseCaseImpl implements InventoryUseCase {

    private final InventoryOutport inventoryOutport;
    private final InventoryStockCache stockCache;

    /** 주문 차감(예약). 순수 Redis 원자 연산 */
    @Override
    public StockReserveResult reserve(String orderId, List<ItemEntry> items) {
        return stockCache.deduct(orderId, items);
    }

    /** 보상 복원. 복원 1회(중복 복원 방지). */
    @Override
    public boolean release(String orderId, List<ItemEntry> items) {
        return stockCache.restore(orderId, items);
    }

    @Transactional
    @Override
    public void decreaseStock(ProductId productId, Quantity quantity) {
        Inventory inventory = inventoryOutport.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(STOCK_NOT_AVAILABLE));

        inventory.decreaseQuantity(quantity.value());
        stockCache.reduce(productId, quantity.value());
    }

    @Transactional
    @Override
    public void increaseStock(ProductId productId, Quantity quantity) {
        Inventory inventory = inventoryOutport.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(STOCK_NOT_AVAILABLE));

        inventory.increaseQuantity(quantity.value());
        stockCache.replenish(productId, quantity.value());
    }

    @Transactional
    @Override
    public void setStock(ProductId productId, Quantity quantity) {
        Inventory inventory = inventoryOutport.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(STOCK_NOT_AVAILABLE));

        inventory.setQuantity(quantity);
        stockCache.set(productId, quantity.value());
    }

    @Transactional(readOnly = true)
    @Override
    public Inventory getStock(ProductId productId) {
        Inventory inventory = inventoryOutport.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(STOCK_NOT_AVAILABLE));

        long live = stockCache.current(productId);
        if (live < 0) {
            stockCache.seedIfAbsent(productId, inventory.getQuantity().value());
        } else {
            inventory.setQuantity(Quantity.create(live));
        }
        return inventory;
    }
}
