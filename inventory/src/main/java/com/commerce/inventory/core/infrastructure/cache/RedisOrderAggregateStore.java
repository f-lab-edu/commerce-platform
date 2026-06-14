package com.commerce.inventory.core.infrastructure.cache;

import com.commerce.inventory.core.application.port.out.OrderAggregateStore;
import com.commerce.shared.vo.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * B2 재집계 Redis HASH 어댑터. Lua를 쓰지 않고 plain 명령(HSET/HLEN/HGETALL/DEL)만 사용한다.
 * 키 네임스페이스(inventory:agg:{orderId})는 B1 키와 분리된다.
 */
@Repository
@RequiredArgsConstructor
public class RedisOrderAggregateStore implements OrderAggregateStore {

    private static final String AGG_PREFIX = "inventory:agg:";
    private static final String FAIL_MARK = "F";
    /** backstop TTL — 정상 흐름은 finalize에서 명시 DEL. */
    private static final Duration AGG_TTL = Duration.ofDays(1);

    private final StringRedisTemplate redis;

    @Override
    public long record(String orderId, ProductId productId, boolean success, long quantity) {
        String key = AGG_PREFIX + orderId;
        String value = success ? String.valueOf(quantity) : FAIL_MARK;
        redis.opsForHash().put(key, productId.id(), value); // HSET (field 단위 멱등)
        redis.expire(key, AGG_TTL);
        Long len = redis.opsForHash().size(key);            // HLEN
        return len == null ? 0L : len;
    }

    @Override
    public Map<String, String> getAll(String orderId) {
        Map<Object, Object> raw = redis.opsForHash().entries(AGG_PREFIX + orderId); // HGETALL
        Map<String, String> result = new HashMap<>();
        raw.forEach((k, v) -> result.put(String.valueOf(k), String.valueOf(v)));
        return result;
    }

    @Override
    public void clear(String orderId) {
        redis.delete(AGG_PREFIX + orderId); // DEL
    }
}
