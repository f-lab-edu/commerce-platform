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
     *
     * <p><b>thread-safe 아님 — 호출자 직렬화 전제.</b> 구현은 HSET과 HLEN을 별개 명령으로 수행하므로
     * 원자적이지 않다. 정확한 finalizer 판정(HLEN == totalItems가 한 번만 성립)은 <b>같은 orderId의
     * 호출이 단일 스레드에서 직렬화될 때만</b> 성립한다. 이는 order-aggregate 토픽의 파티션 키를
     * orderId로 두어 보장한다(Kafka consumer concurrency를 올려도 동일 orderId는 단일 파티션→단일 스레드).
     * 이 전제가 깨지면 무소음으로 정합성이 무너지므로 호출 컨슈머의 파티셔닝을 변경하지 말 것.
     *
     * @param quantity 성공 시 차감 수량(복원에 사용). 실패면 무시되고 "F"로 기록.
     */
    long record(String orderId, ProductId productId, boolean success, long quantity);

    /** HGETALL: productId → "차감수량"(성공) 또는 "F"(실패). */
    Map<String, String> getAll(String orderId);

    /** finalize 후 집계 키 삭제(DEL). */
    void clear(String orderId);
}
