package com.commerce.inventory.core.application.port.out;

import com.commerce.shared.vo.ProductId;

import java.util.Map;

/**
 * B2 주문 단위 재집계 아웃바운드 포트. Redis HASH(inventory:agg:{orderId})만 사용한다(무Lua).
 * field=productId, value=차감수량(성공) 또는 "F"(실패). 도착 수 = HLEN.
 *
 * orderId가 Kafka 파티션 키로 단일 스레드에 직렬화되고 HSET이 field 단위 멱등이므로,
 * 같은 order-aggregate가 재전송돼도 HLEN이 부풀지 않아 finalizer가 정확히 한 번 성립한다.
 */
public interface OrderAggregateStore {

    /**
     * 상품별 결과 기록 후 현재 도착 수(HLEN) 반환.
     * @param quantity 성공 시 차감 수량(복원에 사용). 실패면 무시되고 "F"로 기록.
     */
    long record(String orderId, ProductId productId, boolean success, long quantity);

    /** HGETALL: productId → "차감수량"(성공) 또는 "F"(실패). */
    Map<String, String> getAll(String orderId);

    /** finalize 후 집계 키 삭제(DEL). */
    void clear(String orderId);
}
