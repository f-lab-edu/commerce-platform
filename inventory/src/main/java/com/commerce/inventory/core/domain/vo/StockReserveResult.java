package com.commerce.inventory.core.domain.vo;

import com.commerce.shared.vo.ProductId;

/**
 * 재고 예약(차감) 결과.
 * - SUCCESS      : 이번 호출로 전부 차감됨
 * - INSUFFICIENT : 재고 부족(아무것도 차감되지 않음). failedProductId = 부족했던 상품
 */
public record StockReserveResult(Status status, ProductId failedProductId) {

    public enum Status {
        SUCCESS, INSUFFICIENT
    }

    public static StockReserveResult success() {
        return new StockReserveResult(Status.SUCCESS, null);
    }

    public static StockReserveResult insufficient(ProductId failedProductId) {
        return new StockReserveResult(Status.INSUFFICIENT, failedProductId);
    }

    /** 차감이 완료된 상태면 후속 saga를 진행해도 안전하다. */
    public boolean isReserved() {
        return status == Status.SUCCESS;
    }
}
