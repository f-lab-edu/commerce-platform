package com.commerce.payments.core.domain.enums;

/**
 * PG 승인 처리 결과 유형
 * 이벤트 계약(PgResult)과 같은 이름을 사용한다.
 */
public enum PgApprovalStatus {
    /** 승인 성공 + 요청금액 일치 */
    APPROVED,
    /** 승인 거절 */
    DECLINED,
    /** 승인 결과 불명 → 망취소 성공 */
    UNKNOWN_VOIDED,
    /** 승인 성공 + 요청금액 불일치 → 망취소 성공 */
    AMOUNT_MISMATCH_VOIDED,
    /** 망취소 실패 (수동 처리 대상) */
    VOID_FAILED
}
