package com.commerce.inventory.core.infrastructure.persistence;

import com.commerce.inventory.core.domain.aggregate.Inventory;
import com.commerce.shared.vo.ProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JPA 재고 Repository
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, ProductId> {
    Optional<Inventory> findByProductId(ProductId productId);

    /** B2 조건부 차감. 재고가 충분할 때만 1행 갱신. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Inventory i SET i.quantity.value = i.quantity.value - :qty, i.updatedAt = :now " +
           "WHERE i.productId = :productId AND i.quantity.value >= :qty")
    int deductIfEnough(@Param("productId") ProductId productId,
                       @Param("qty") long qty,
                       @Param("now") LocalDateTime now);

    /** B2 보상 복원. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Inventory i SET i.quantity.value = i.quantity.value + :qty, i.updatedAt = :now " +
           "WHERE i.productId = :productId")
    int replenish(@Param("productId") ProductId productId,
                  @Param("qty") long qty,
                  @Param("now") LocalDateTime now);
}
