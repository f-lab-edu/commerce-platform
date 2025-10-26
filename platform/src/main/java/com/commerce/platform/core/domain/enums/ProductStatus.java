package com.commerce.platform.core.domain.enums;

import com.commerce.platform.core.domain.vo.Quantity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    ACTIVE("판매중"),
    LOW_STOCK("품절임박"),
    OUT_OF_STOCK("품절"),
    DISCONTINUED("판매중지");

    private final String description;

    private static final int LOW_STOCK_THRESHOLD = 10;

    /**
     * 재고 수량에 따라 상태 결정
     */
    public static ProductStatus fromStockQuantity(Quantity stockQuantity) {
        if (stockQuantity.getValue() == 0) {
            return OUT_OF_STOCK;
        }
        if (stockQuantity.getValue() <= LOW_STOCK_THRESHOLD) {
            return LOW_STOCK;
        }
        return ACTIVE;
    }
}
