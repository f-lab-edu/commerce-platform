package com.commerce.shared.kafka.event.type;

/**
 * payment.pg-result 결과 유형.
 * APPROVED / CANCELED 는 성공한 PG 거래(payment 저장), 나머지는 실패 거래(payment_failure 저장).
 */
public enum PgResult {
    /** 승인 성공 + 요청금액 일치 */
    APPROVED,
    /** 승인 거절 */
    DECLINED,
    /** 승인 결과 불명 → 망취소 성공 */
    UNKNOWN_VOIDED,
    /** 승인 성공 + 요청금액 불일치 → 망취소 성공 */
    AMOUNT_MISMATCH_VOIDED,
    /** 망취소 실패 (수동 처리 대상) */
    VOID_FAILED,
    /** 결제취소(환불) 성공 */
    CANCELED,
    /** 결제취소(환불) 실패 */
    REFUND_FAILED
}
