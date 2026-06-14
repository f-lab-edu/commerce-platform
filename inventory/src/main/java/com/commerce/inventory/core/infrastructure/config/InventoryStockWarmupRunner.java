package com.commerce.inventory.core.infrastructure.config;

import com.commerce.inventory.core.application.port.out.InventoryStockCache;
import com.commerce.inventory.core.domain.aggregate.Inventory;
import com.commerce.inventory.core.infrastructure.persistence.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 부팅 시 DB 재고를 Redis 카운터로 적재(warmup). 이미 라이브 값이 있으면 보존(SETNX).
 *
 * Redis가 차감의 동기 게이트(단일 진실원천)이고 DB는 {@link com.commerce.inventory.bootstrap.event.InventoryLedgerConsumer}가
 * 비동기로 갱신하는 durable 원장이다. 따라서 DB는 더 이상 stale 원본이 아니라 실제 차감이 반영된 잔량이며,
 * Redis 키가 유실된 경우 이 잔량으로 재시드하면(SETNX) 오버셀 폭이 원본 전량이 아니라 미영속 차감분으로 줄어든다.
 *
 * 잔여 윈도우(운영 주의): 원장이 아직 영속화하지 못한 in-flight 차감만큼 DB가 Redis보다 잠깐 높을 수 있어,
 * 유실 직후 재시드 시 그 분량만큼 과다 적재될 수 있다. 완전 제거하려면 미영속 inventory.deducted를
 * 재시드 후 Redis에 리플레이해야 한다(후속 과제).
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class InventoryStockWarmupRunner implements ApplicationRunner {

    private final InventoryRepository inventoryRepository;
    private final InventoryStockCache stockCache;

    @Override
    public void run(ApplicationArguments args) {
        List<Inventory> all = inventoryRepository.findAll();
        all.forEach(inv -> stockCache.seedIfAbsent(inv.getProductId(), inv.getQuantity().value()));
        log.info("[Inventory] Redis 재고 warmup 완료 - {}건", all.size());
    }
}
