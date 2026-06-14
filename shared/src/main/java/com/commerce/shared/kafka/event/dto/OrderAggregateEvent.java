package com.commerce.shared.kafka.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * B2 scatter-gather: 주문 단위 재집계 신호 (key = orderId).
 *
 * 상품별 차감 결과를 주문 단위로 모은다. InventoryOrderAggregateConsumer가 Redis HASH에
 * HSET(field=productId, value=차감수량 또는 "F")로 기록하고 HLEN == totalItems가 되는 유일
 * finalizer에서 주문 완료/보상을 판정한다. 같은 orderId가 Kafka 파티션 키로 단일 스레드에 직렬화된다.
 *
 * - quantity : 차감 성공 시의 차감 수량(부분 실패 롤백 REPLENISH에 사용). 실패면 의미 없음.
 * - customerId/couponId/payMethod/payProvider : stock-command가 전달한 주문 컨텍스트 스칼라.
 *   finalize에서 inventory.deducted 재구성에 사용한다(별도 저장 없음).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderAggregateEvent(
        String orderId,
        String productId,
        long quantity,
        boolean success,
        int totalItems,
        String customerId,
        String couponId,
        String payMethod,
        String payProvider,
        String key,
        LocalDateTime timestamp
) implements DomainEvent { }
