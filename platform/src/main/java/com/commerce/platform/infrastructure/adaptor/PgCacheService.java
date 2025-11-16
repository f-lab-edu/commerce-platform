package com.commerce.platform.infrastructure.adaptor;

import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.enums.PgProvider;
import com.commerce.platform.infrastructure.persistence.PgFeeInfo;
import com.commerce.platform.infrastructure.persistence.PgFeeInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * PG 라우팅을 위한 Redis 캐시 서비스
 * 수수료 낮은 순으로 정렬된 PG 목록 관리
 * 장애 PG는 자동으로 제외
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PgCacheService {
    
    private final StringRedisTemplate redisTemplate;
    private final PgFeeInfoRepository feeInfoRepository;
    
    private static final String ROUTE_KEY_PREFIX = "pg:route:";
    private static final String HEALTH_KEY_PREFIX = "pg:health:";

    public PgProvider getBestPg(PayMethod payMethod, PayProvider payProvider, List<PgProvider> supportedPgs) {
        // redis 조회
        Set<String> pgProviders = getAvailablePgsFromCache(payMethod, payProvider);

        // miss
        if (pgProviders == null || pgProviders.isEmpty()) {
            pgProviders = refreshCache(payMethod, payProvider);
        }
        
        // 장애 PG 제외 첫번째 선택
        PgProvider bestPg = null;
        for (String pgName : pgProviders) {
            bestPg = PgProvider.getByPgName(pgName);
            if (supportedPgs.contains(bestPg) && isHealthy(bestPg)) {
                return bestPg;
            }
        }
        
        log.error("모든 PG 장애 중: payMethod={}, payProvider={}", payMethod, payProvider);
        return null;
    }
    
    /**
     * ZSet 수수료 asc
     */
    private Set<String> getAvailablePgsFromCache(PayMethod payMethod, PayProvider payProvider) {
        String key = buildRouteKey(payMethod, payProvider);
        return redisTemplate.opsForZSet().range(key, 0, -1);
    }
    
    /**
     * DB에서 수수료 조회 및 Redis 캐싱
     * ZSet score :수수료율
     */
    public Set<String> refreshCache(PayMethod payMethod, PayProvider payProvider) {
        String key = buildRouteKey(payMethod, payProvider);
        // DB 조회: 수수료 낮은 순
        List<PgFeeInfo> configs = feeInfoRepository
            .findByPayMethodAndPayProvider(payMethod, payProvider);

        // todo 별도 스레드로 하는것이 좋을지
        // 기존 캐시 삭제
        redisTemplate.delete(key);
        
        for (PgFeeInfo config : configs) {
            redisTemplate.opsForZSet().add(
                    key,
                    config.getPgProvider().name(),
                    config.getFeeRate().doubleValue()
            );
        }

        return configs.stream()
                .sorted(Comparator.comparing(PgFeeInfo::getFeeRate))
                .map(pgFeeInfo -> pgFeeInfo.getPgProvider().name())
                .collect(Collectors.toSet());
    }
    
    /**
     * PG 헬스 체크
     */
    public boolean isHealthy(PgProvider pgProvider) {
        String healthKey = HEALTH_KEY_PREFIX + pgProvider.name();
        return redisTemplate.opsForValue().get(healthKey) == null;
    }
    
    /**
     * PG 장애
     * TTL : 30m
     */
    public void markPgAsUnhealthy(PgProvider pgProvider) {
        String healthKey = HEALTH_KEY_PREFIX + pgProvider.name();
        redisTemplate.opsForValue().set(healthKey, "ERROR", 30, TimeUnit.MINUTES);
    }

    /**
     * Redis Key 생성: pg:route:CARD:SHIN_HAN
     */
    private String buildRouteKey(PayMethod payMethod, PayProvider payProvider) {
        return ROUTE_KEY_PREFIX + payMethod.name() + ":" + payProvider.name();
    }
}
