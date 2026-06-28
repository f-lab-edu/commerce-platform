package com.commerce.inventory.core.application.port.out;

import com.commerce.inventory.core.domain.vo.StockReserveResult;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.ProductId;

import java.util.List;

/**
 * Redis 기반 재고 카운터 아웃바운드 포트.
 *
 * 모든 변경(주문 차감/복원, 관리자·고객사 충당)은 동일한 키 inventory:stock:{productId} 에 대한
 * 원자 연산으로 수렴한다. 멀티상품 차감은 단일 Lua로 all-or-nothing 처리한다.
 */
public interface InventoryStockCache {

    /** 주문의 멀티상품 전체를 원자적으로 차감한다(전부 충분할 때만 전부 차감). 멱등성 미보장(추후 outbox). */
    StockReserveResult deduct(String orderId, List<ItemEntry> items);

    /** 주문 차감분을 원복한다(복원 1회, 중복 복원 방지). 실제 복원 시 true. */
    boolean restore(String orderId, List<ItemEntry> items);

    /** 충당(top-up). 관리자/고객사 재고 충전. 원자 INCRBY. */
    void replenish(ProductId productId, long quantity);

    /** 관리자 차감(보정). 재고가 충분할 때만 차감, 성공 시 true. */
    boolean reduce(ProductId productId, long quantity);

    /** 관리자 절대 설정(재고 정정). 라이브 카운터를 덮어쓴다. */
    void set(ProductId productId, long quantity);

    /** 부팅 warmup/lazy 시드: 키가 없을 때만 DB 수량으로 채운다(SETNX). */
    void seedIfAbsent(ProductId productId, long quantity);

    /** 현재 라이브 재고. 키가 없으면 -1. */
    long current(ProductId productId);
}
