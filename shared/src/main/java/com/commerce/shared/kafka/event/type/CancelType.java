package com.commerce.shared.kafka.event.type;

/**
 * payment.pg-cancel 취소 유형. Kafka 헤더 {@link EventHeaders#CANCEL_TYPE}로 전달한다.
 */
public enum CancelType {
    /** 망취소: 승인 실패(결과 불명, 요청금액 불일치) 처리 */
    VOID,
    /** 결제취소: 정상 승인 거래의 환불 */
    REFUND
}
