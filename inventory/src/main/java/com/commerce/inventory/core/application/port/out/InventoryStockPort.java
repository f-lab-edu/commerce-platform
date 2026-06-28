package com.commerce.inventory.core.application.port.out;

import com.commerce.shared.vo.ProductId;

/**
 * 재고 차감/복원 아웃바운드 포트. 단일 조건부 UPDATE(SELECT 없음)로 DB 재고를 다룬다.
 * 락 없이 행 원자성만으로 오버셀을 막는다(부족하면 affected == 0).
 */
public interface InventoryStockPort {

    /**
     * 조건부 차감: UPDATE inventory SET quantity = quantity - qty WHERE productId = ? AND quantity >= qty.
     * @return affected row 수 (1 = 성공, 0 = 재고 부족 또는 상품 없음)
     */
    int deductIfEnough(ProductId productId, long quantity);

    /**
     * 무조건 복원(보상): UPDATE inventory SET quantity = quantity + qty WHERE productId = ?.
     * @return affected row 수 (보통 1; 0이면 상품 행 없음)
     */
    int replenish(ProductId productId, long quantity);
}
