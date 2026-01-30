package com.commerce.payments.infrastructure.adaptor;

import com.commerce.payments.core.domain.enums.PgProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 장애 PG  캐싱
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PgCacheService {
    
    private final StringRedisTemplate redisTemplate;

    private static final String HEALTH_KEY_PREFIX = "pg:health:";

    /**
     * PG 헬스 체크
     */
    public boolean isHealthy(PgProvider pgProvider) {
        String healthKey = HEALTH_KEY_PREFIX + pgProvider.name();
        return redisTemplate.opsForValue().get(healthKey) == null;
    }
    
    /**
     * PG 장애
     * TTL: 30m
     */
    public void markPgAsUnhealthy(PgProvider pgProvider) {
        String healthKey = HEALTH_KEY_PREFIX + pgProvider.name();
        redisTemplate.opsForValue().set(healthKey, "ERROR", 30, TimeUnit.MINUTES);
    }
}
