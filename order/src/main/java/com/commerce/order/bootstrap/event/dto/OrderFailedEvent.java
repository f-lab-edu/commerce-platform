package com.commerce.order.bootstrap.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Order 모듈 관점의 주문 실패 이벤트 페이로드.
 *
 * inventory.deduct-failed / order.price-failed / coupon.apply-failed / payment.failed
 * 4개 토픽이 서로 다른 producer 이벤트 구조로 발행되지만, order는 주문 취소를 위해
 * orderId 만 필요하므로 단일 투영 DTO로 통합한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderFailedEvent(String orderId) { }
