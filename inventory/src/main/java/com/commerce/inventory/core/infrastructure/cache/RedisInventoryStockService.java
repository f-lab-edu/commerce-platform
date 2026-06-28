package com.commerce.inventory.core.infrastructure.cache;

import com.commerce.inventory.core.application.port.out.InventoryOutport;
import com.commerce.inventory.core.application.port.out.InventoryStockCache;
import com.commerce.inventory.core.domain.vo.StockReserveResult;
import com.commerce.shared.exception.BusinessError;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.ProductId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis 카운터 기반 재고 어댑터(B1). 멀티상품 차감을 단일 Lua로 all-or-nothing 처리한다.
 */
@Slf4j
@Repository
public class RedisInventoryStockService implements InventoryStockCache {

    private static final String STOCK_PREFIX = "inventory:stock:";
    private static final String RESTORED_PREFIX = "inventory:restored:";
    private static final String MARKER_TTL_SECONDS = String.valueOf(7L * 24 * 60 * 60); // 7일 (복원 마커 전용)

    private static final long RET_SUCCESS = 1L;
    private static final long RET_NOT_LOADED = -1L;
    private static final long INSUFFICIENT_BASE = -100L; // -(100 + itemIndex)

    private final StringRedisTemplate redis;
    private final InventoryOutport inventoryOutport;

    private final RedisScript<Long> deductScript = load("redis/deduct.lua");
    private final RedisScript<Long> restoreScript = load("redis/restore.lua");
    private final RedisScript<Long> reduceScript = load("redis/reduce.lua");

    public RedisInventoryStockService(StringRedisTemplate redis, InventoryOutport inventoryOutport) {
        this.redis = redis;
        this.inventoryOutport = inventoryOutport;
    }

    private static RedisScript<Long> load(String path) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(path));
        script.setResultType(Long.class);
        return script;
    }

    @Override
    public StockReserveResult deduct(String orderId, List<ItemEntry> items) {
        long ret = nz(runDeduct(items));

        if (ret == RET_NOT_LOADED) {
            // 키 누락 → DB에서 시드 후 1회 재시도
            items.forEach(it -> seedFromDb(it.productId()));
            ret = nz(runDeduct(items));
        }

        if (ret == RET_SUCCESS) {
            return StockReserveResult.success();
        }
        if (ret == RET_NOT_LOADED) {
            // 시드 후에도 미적재 = 재고 자체가 없는 상품
            throw new BusinessException(BusinessError.STOCK_NOT_AVAILABLE);
        }
        // INSUFFICIENT: ret == -(100 + index)
        int index = (int) (-ret + INSUFFICIENT_BASE); // -ret - 100
        ProductId failed = items.get(index - 1).productId();
        log.warn("[Inventory] 재고 부족 - orderId: {}, productId: {}", orderId, failed.id());
        return StockReserveResult.insufficient(failed);
    }

    private Long runDeduct(List<ItemEntry> items) {
        List<String> keys = new ArrayList<>(items.size());
        items.forEach(it -> keys.add(stockKey(it.productId())));

        Object[] args = new Object[items.size()];
        for (int i = 0; i < items.size(); i++) {
            args[i] = String.valueOf(items.get(i).quantity().value());
        }
        return redis.execute(deductScript, keys, args);
    }

    @Override
    public boolean restore(String orderId, List<ItemEntry> items) {
        List<String> keys = new ArrayList<>(items.size() + 1);
        items.forEach(it -> keys.add(stockKey(it.productId())));
        keys.add(RESTORED_PREFIX + orderId);

        Object[] args = new Object[items.size() + 1];
        args[0] = MARKER_TTL_SECONDS;
        for (int i = 0; i < items.size(); i++) {
            args[i + 1] = String.valueOf(items.get(i).quantity().value());
        }
        Long ret = redis.execute(restoreScript, keys, args);
        return ret != null && ret == 1L;
    }

    @Override
    public void replenish(ProductId productId, long quantity) {
        // 순수 delta(INCRBY). 키는 warmup/seed로 이미 존재한다고 가정(없으면 quantity로 생성).
        redis.opsForValue().increment(stockKey(productId), quantity);
    }

    @Override
    public boolean reduce(ProductId productId, long quantity) {
        Long ret = redis.execute(reduceScript, List.of(stockKey(productId)), String.valueOf(quantity));
        return ret != null && ret == 1L;
    }

    @Override
    public void set(ProductId productId, long quantity) {
        redis.opsForValue().set(stockKey(productId), String.valueOf(quantity));
    }

    @Override
    public void seedIfAbsent(ProductId productId, long quantity) {
        redis.opsForValue().setIfAbsent(stockKey(productId), String.valueOf(quantity));
    }

    @Override
    public long current(ProductId productId) {
        String v = redis.opsForValue().get(stockKey(productId));
        return v == null ? -1L : Long.parseLong(v);
    }

    /** DB에 재고가 있으면 Redis 키가 없을 때만 시드한다. */
    private void seedFromDb(ProductId productId) {
        inventoryOutport.findByProductId(productId)
                .ifPresent(inv -> seedIfAbsent(productId, inv.getQuantity().value()));
    }

    private String stockKey(ProductId productId) {
        return STOCK_PREFIX + productId.id();
    }

    /** Lua 스크립트 결과 null 방어: 연결 이상 등으로 null이면 NOT_LOADED로 간주해 시드/재시도 경로를 탄다. */
    private static long nz(Long ret) {
        return ret == null ? RET_NOT_LOADED : ret;
    }
}
