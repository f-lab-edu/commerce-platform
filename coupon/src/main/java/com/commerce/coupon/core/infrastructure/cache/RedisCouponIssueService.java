package com.commerce.coupon.core.infrastructure.cache;

import com.commerce.coupon.core.application.port.out.CouponIssueCache;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * Redis에 쿠폰 발급된 사용자를 관리한다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisCouponIssueService implements CouponIssueCache {
    
    private static final String KEY_PREFIX = "coupon:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public void save(CouponId couponId, CustomerId customerId) {
        String key = generateKey(couponId);
        redisTemplate.opsForSet().add(key, customerId.id());
        redisTemplate.expire(key, Duration.ofDays(7));
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
