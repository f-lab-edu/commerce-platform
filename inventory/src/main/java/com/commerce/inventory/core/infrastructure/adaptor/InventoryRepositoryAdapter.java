package com.commerce.inventory.core.infrastructure.adaptor;

import com.commerce.inventory.core.application.port.out.InventoryOutport;
import com.commerce.inventory.core.application.port.out.InventoryStockPort;
import com.commerce.inventory.core.domain.aggregate.Inventory;
import com.commerce.inventory.core.infrastructure.persistence.InventoryRepository;
import com.commerce.shared.vo.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA를 사용하여 재고 저장소 포트를 구현하는 어댑터.
 * 관리자용 {@link InventoryOutport}와 조건부 차감용 {@link InventoryStockPort}를 함께 구현한다.
 */
@Component
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryOutport, InventoryStockPort {

    private final InventoryRepository inventoryRepository;

    @Override
    public Optional<Inventory> findByProductId(ProductId productId) {
        return inventoryRepository.findByProductId(productId);
    }

    @Override
    public void save(Inventory inventory) {
        inventoryRepository.save(inventory);
    }

    @Override
    public void saveAll(List<Inventory> inventories) {
        inventoryRepository.saveAll(inventories);
    }

    @Override
    public int deductIfEnough(ProductId productId, long quantity) {
        return inventoryRepository.deductIfEnough(productId, quantity, LocalDateTime.now());
    }

    @Override
    public int replenish(ProductId productId, long quantity) {
        return inventoryRepository.replenish(productId, quantity, LocalDateTime.now());
    }
}
