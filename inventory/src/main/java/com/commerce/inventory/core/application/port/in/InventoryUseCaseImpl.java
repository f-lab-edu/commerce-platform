package com.commerce.inventory.core.application.port.in;

import com.commerce.inventory.core.application.port.out.InventoryOutport;
import com.commerce.inventory.core.domain.aggregate.Inventory;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.commerce.shared.exception.BusinessError.STOCK_NOT_AVAILABLE;

@RequiredArgsConstructor
@Service
public class InventoryUseCaseImpl implements InventoryUseCase {

    private final InventoryOutport inventoryOutport;

    @Transactional
    @Override
    public void decreaseStock(ProductId productId, Quantity quantity) {
        Inventory inventory = inventoryOutport.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(STOCK_NOT_AVAILABLE));

        inventory.decreaseQuantity(quantity.value());
        inventoryOutport.save(inventory);
    }

    @Transactional
    @Override
    public void increaseStock(ProductId productId, Quantity quantity) {
        Inventory inventory = inventoryOutport.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(STOCK_NOT_AVAILABLE));

        inventory.increaseQuantity(quantity.value());
        inventoryOutport.save(inventory);
    }

    @Transactional
    @Override
    public void setStock(ProductId productId, Quantity quantity) {
        Inventory inventory = inventoryOutport.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(STOCK_NOT_AVAILABLE));

        inventory.setQuantity(quantity);
        inventoryOutport.save(inventory);
    }

    @Transactional(readOnly = true)
    @Override
    public Inventory getStock(ProductId productId) {
        return inventoryOutport.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(STOCK_NOT_AVAILABLE));
    }
}