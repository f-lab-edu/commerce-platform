package com.commerce.inventory.infrastructure.adaptor;

import com.commerce.inventory.application.port.out.InventoryOutport;
import com.commerce.inventory.domain.aggregate.Inventory;
import com.commerce.inventory.infrastructure.persistence.InventoryRepository;
import com.commerce.shared.vo.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * JPA를 사용하여 재고 저장소 포트를 구현하는 어댑터
 */
@Component
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryOutport {

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

}
