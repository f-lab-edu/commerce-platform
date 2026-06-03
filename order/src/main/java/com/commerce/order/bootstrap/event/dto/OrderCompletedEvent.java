package com.commerce.order.bootstrap.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Order 모듈 관점의 주문 완료 이벤트 페이로드.
 *
 * payment.completed 토픽으로 들어오는 producer 이벤트를 order 모듈이 필요한 필드만
 * 투영(projection)해서 수신한다. producer 측 이벤트가 추가 필드를 가져도 무시.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCompletedEvent(
        String orderId,
        long originAmt,
        long discountAmt
) { }
