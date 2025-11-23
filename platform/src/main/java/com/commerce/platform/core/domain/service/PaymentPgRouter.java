package com.commerce.platform.core.domain.service;

import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.enums.PgProvider;
import com.commerce.platform.infrastructure.adaptor.PgCacheService;
import com.commerce.platform.infrastructure.persistence.PgFeeInfo;
import com.commerce.platform.infrastructure.persistence.PgFeeInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
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
    private final PgFeeInfoRepository feeInfoRepository;
    //결제방식 + 카드사/통신사 별 수수료기준 정렬됨
    private Map<PayMethod, Map<PayProvider, TreeSet<PgFeeInfo>>> pgFeeCache = null;

    public PaymentPgRouter(List<PgStrategy> list, PgCacheService pgCacheService, PgFeeInfoRepository feeInfoRepository) {
        this.pgStrategies = list.stream()
                .collect(Collectors.toMap(PgStrategy::getPgProvider, pg -> pg));
        this.pgCacheService = pgCacheService;
        this.feeInfoRepository = feeInfoRepository;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void initPgCache() {
        setPgFeeCache();
    }

    /**
     * 결제유형 + 카드사 => 유효 PG 추출
     *  redis 캐싱된 health check
     */
    public PgStrategy routePg(PayMethod payMethod, PayProvider payProvider) {
        PgProvider selectedPg = pgFeeCache.get(payMethod).get(payProvider)
                .stream()
                .filter(pgFeeInfo -> pgCacheService.isHealthy(pgFeeInfo.getPgProvider()))
                .toList()
                .getFirst()
                .getPgProvider();
        
        if (selectedPg == null) {
            throw new IllegalStateException("현재 사용 가능한 PG사가 없습니다");
        }
        
        return pgStrategies.get(selectedPg);
    }

    /**
     * PG Provider => Strategy bean 추출
     */
    public PgStrategy getPgStrategyByProvider(PgProvider pgProvider) {
        PgStrategy strategy = pgStrategies.get(pgProvider);
        if (strategy == null) {
            throw new IllegalArgumentException("존재하지 않는 PG: " + pgProvider);
        }
        return strategy;
    }

    @Scheduled(cron = "0 * * * * *")
    private void refreshPgCache() {
        setPgFeeCache();
    }

    private void setPgFeeCache() {
        pgFeeCache = feeInfoRepository.findAllActiveAndValid()
                .stream()
                .collect(Collectors.groupingBy(PgFeeInfo::getPayMethod,
                        Collectors.groupingBy(PgFeeInfo::getPayProvider,
                                Collectors.toCollection(() ->
                                        new TreeSet<>(Comparator.comparing(PgFeeInfo::getFeeRate))
                                )
                        )));
    }
}
