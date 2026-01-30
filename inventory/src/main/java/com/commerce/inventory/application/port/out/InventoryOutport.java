package com.commerce.inventory.application.port.out;

import com.commerce.inventory.domain.aggregate.Inventory;
import com.commerce.shared.vo.ProductId;

import java.util.List;
import java.util.Optional;

/**
 * 재고 저장소 아웃바운드 포트
 * 핵사고날 아키텍처: Application Layer에서 정의, Infrastructure Layer에서 구현
 */
public interface InventoryOutport {
    Optional<Inventory> findByProductId(ProductId productId);
    void save(Inventory inventory);
    void saveAll(List<Inventory> inventories);
}
