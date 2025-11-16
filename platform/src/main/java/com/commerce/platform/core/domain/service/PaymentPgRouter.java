package com.commerce.platform.core.domain.service;

import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.enums.PgProvider;
import com.commerce.platform.infrastructure.adaptor.PgCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PG 라우팅 서비스
 * 결제방식 + 카드사/통신사 => PG
 * return 서버 정상 + 수수료 가장 저렴한 PG
 */
@Slf4j
@Service
public class PaymentPgRouter {
    
    private final Map<PgProvider, PgStrategy> pgStrategies;
    private final PgCacheService pgCacheService;

    public PaymentPgRouter(List<PgStrategy> list, PgCacheService pgCacheService) {
        this.pgStrategies = list.stream()
                .collect(Collectors.toMap(PgStrategy::getPgProvider, pg -> pg));
        this.pgCacheService = pgCacheService;
    }

    /**
     * 결제유형+카드사에 따라 PG 선택
     * Redis에서 캐싱
     */
    public PgStrategy routePg(PayMethod payMethod, PayProvider payProvider) {
        
        List<PgProvider> supportedPgs = PgProvider.getByPayMethod(payMethod, payProvider);

        PgProvider selectedPg = pgCacheService.getBestPg(payMethod, payProvider, supportedPgs);
        
        if (selectedPg == null) {
            throw new IllegalStateException("현재 사용 가능한 PG사가 없습니다");
        }
        
        return pgStrategies.get(selectedPg);
    }

    /**
     * PG Provider => Strategy 조회
     */
    public PgStrategy getPgStrategyByProvider(PgProvider pgProvider) {
        PgStrategy strategy = pgStrategies.get(pgProvider);
        if (strategy == null) {
            throw new IllegalArgumentException("존재하지 않는 PG: " + pgProvider);
        }
        return strategy;
    }
}
