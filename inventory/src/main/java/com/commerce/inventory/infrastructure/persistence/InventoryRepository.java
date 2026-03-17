package com.commerce.inventory.infrastructure.persistence;

import com.commerce.inventory.domain.aggregate.Inventory;
import com.commerce.shared.vo.ProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA 재고 Repository
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, ProductId> {
    Optional<Inventory> findByProductId(ProductId productId);
}
