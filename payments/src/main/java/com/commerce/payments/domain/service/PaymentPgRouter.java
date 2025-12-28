package com.commerce.payments.domain.service;

import com.commerce.payments.application.port.out.PgStrategy;
import com.commerce.payments.domain.enums.PayMethod;
import com.commerce.shared.enums.PayProvider;
import com.commerce.payments.domain.enums.PgProvider;
import com.commerce.payments.infrastructure.adaptor.PgCacheService;
import com.commerce.payments.infrastructure.persistence.PgFeeInfo;
import com.commerce.payments.infrastructure.persistence.PgFeeInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * PG 라우팅 서비스
 * 결제방식 + 카드사/통신사 => PG
 * return 서버 정상 + 수수료 가장 저렴한 PG
 */
@Slf4j
@Service
public class PaymentPgRouter {
    
    private final Map<PgProvider, Map<PayMethod, PgStrategy>> pgStrategies;
    private final PgCacheService pgCacheService;
    private final PgFeeInfoRepository feeInfoRepository;
    //결제방식 + 카드사/통신사 별 수수료기준 정렬됨
    private Map<PayMethod, Map<PayProvider, TreeSet<PgFeeInfo>>> pgFeeCache = null;

    public PaymentPgRouter(List<PgStrategy> list, PgCacheService pgCacheService, PgFeeInfoRepository feeInfoRepository) {
        this.pgStrategies = list.stream()
                .collect(Collectors.groupingBy(
                        PgStrategy::getPgProvider,
                        Collectors.toMap(
                                PgStrategy::getPgPayMethod,
                                Function.identity()
                        )
                ));
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
        
        return pgStrategies.get(selectedPg).get(payMethod);
    }

    /**
     * PG Provider => Strategy 조회
     */
    public PgStrategy getPgStrategyByProvider(PgProvider pgProvider, PayMethod payMethod) {
        PgStrategy strategy = pgStrategies.get(pgProvider).get(payMethod);
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
