package com.commerce.shared.kafka.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * B2 scatter-gather: 상품 단위 재고 커맨드 (key = productId).
 *
 * fanout이 주문을 상품 단위로 분해해 발행하고, InventoryStockCommandConsumer가 type으로 분기 처리한다.
 * 같은 productId가 Kafka 파티션 키로 단일 컨슈머 스레드에 직렬화되어, DB 조건부 UPDATE의 행 원자성만으로
 * 오버셀이 없다.
 *
 * - quantity   : 차감/복원 수량
 * - totalItems : 해당 주문의 상품 종류 수(재집계 임계값). DEDUCT+orderId에서만 의미 있으며,
 *                보상 REPLENISH 커맨드는 재집계를 발행하지 않으므로 0으로 채운다.
 * - customerId/couponId/payMethod/payProvider : 주문 레벨 컨텍스트 스칼라. fanout이 실어 보내
 *   order-aggregate까지 전달돼 finalize 시 inventory.deducted 재구성에 쓰인다(별도 저장 없음).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StockCommandEvent(
        StockCommandType type,
        String orderId,
        String productId,
        long quantity,
        int totalItems,
        String customerId,
        String couponId,
        String payMethod,
        String payProvider,
        String key,
        LocalDateTime timestamp
) implements DomainEvent { }
