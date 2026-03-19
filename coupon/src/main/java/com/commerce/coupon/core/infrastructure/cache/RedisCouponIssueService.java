package com.commerce.coupon.core.infrastructure.cache;

import com.commerce.coupon.core.application.port.out.CouponIssueCache;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.Collections;

/**
 * Redis에 쿠폰 발급된 사용자를 관리한다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisCouponIssueService implements CouponIssueCache {
    
    private static final String KEY_PREFIX = "coupon:";

    private static final DefaultRedisScript<Long> SADD_EXPIRE_SCRIPT = new DefaultRedisScript<>(
            "redis.call('SADD', KEYS[1], ARGV[1]) " +
            "redis.call('EXPIRE', KEYS[1], ARGV[2]) " +
            "return 1",
            Long.class
    );

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(CouponId couponId, CustomerId customerId) {
        try {
            String key = generateKey(couponId);
            redisTemplate.execute(SADD_EXPIRE_SCRIPT,
                    Collections.singletonList(key),
                    customerId.id(),  7 * 24 * 60 * 60); // 우선 7일
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isIssued(CouponId couponId, CustomerId customerId) {
        String key = generateKey(couponId);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, customerId.id()));
    }

    private String generateKey(CouponId couponId) {
        return new StringBuilder(KEY_PREFIX).append(couponId.id()).toString();
    }
}
