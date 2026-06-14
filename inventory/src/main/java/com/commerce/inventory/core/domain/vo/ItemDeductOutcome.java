package com.commerce.inventory.core.domain.vo;

/**
 * B2 상품 단위 차감 결과(DB 조건부 UPDATE affected row 기반).
 * - SUCCESS      : affected == 1 → 차감됨
 * - INSUFFICIENT : affected == 0 → 재고 부족, 아무것도 차감되지 않음
 *
 * 재전송 시 재차감 방지(멱등)는 본 스펙 범위 밖(추후 일괄 적용).
 */
public enum ItemDeductOutcome {
    SUCCESS,
    INSUFFICIENT;

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /** affected row 수(1/0)로 결과 매핑. */
    public static ItemDeductOutcome fromAffected(int affectedRows) {
        return affectedRows == 1 ? SUCCESS : INSUFFICIENT;
    }
}
