package com.commerce.platform.core.domain.service;

import com.commerce.platform.PlatformApplication;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.enums.PgProvider;
import com.commerce.platform.infrastructure.adaptor.PgCacheService;
import com.commerce.platform.infrastructure.persistence.PgFeeInfo;
import com.commerce.platform.infrastructure.persistence.PgFeeInfoRepository;
import com.commerce.platform.infrastructure.pg.NHNStrategy;
import com.commerce.platform.infrastructure.pg.TossStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * PaymentPgRouter 단위테스트
 */
@ExtendWith(MockitoExtension.class)
class PaymentPgRouterTest {
    private PaymentPgRouter paymentPgRouter;

    @Mock
    private PgCacheService pgCacheService;

    @Mock
    private PgFeeInfoRepository feeInfoRepository;

    @BeforeEach
    void init_paymentPgRouter() {
        // mock pgStrategy
        TossStrategy tossMock = mock(TossStrategy.class);
        NHNStrategy nhnMock   = mock(NHNStrategy.class);
        
        when(tossMock.getPgProvider()).thenReturn(PgProvider.TOSS);
        when(nhnMock.getPgProvider()).thenReturn(PgProvider.NHN);
        
        // PaymentPgRouter 생성
        paymentPgRouter = new PaymentPgRouter(
                List.of(tossMock, nhnMock),
                pgCacheService,
                feeInfoRepository
        );

        // mock 수수료
        PgFeeInfo tossFee_kb = new PgFeeInfo(
                PgProvider.TOSS, PayMethod.CARD, PayProvider.KB,
                BigDecimal.valueOf(2.5),
                LocalDate.now(), LocalDate.now().plusMonths(1)
        );

        PgFeeInfo tossFee_samsung = new PgFeeInfo(
                PgProvider.TOSS, PayMethod.CARD, PayProvider.SAMSUNG,
                BigDecimal.valueOf(2.6),
                LocalDate.now(), LocalDate.now().plusMonths(1)
        );

        PgFeeInfo nhnFee_samsung = new PgFeeInfo(
                PgProvider.NHN, PayMethod.CARD, PayProvider.SAMSUNG,
                BigDecimal.valueOf(2.3),
                LocalDate.now(), LocalDate.now().plusMonths(1)
        );

        when(feeInfoRepository.findAllActiveAndValid())
                .thenReturn(List.of(tossFee_kb, tossFee_samsung, nhnFee_samsung));

        // 캐시 초기화 @EventListener(ApplicationReadyEvent.class)
        paymentPgRouter.initPgCache();
    }

    @DisplayName("결제유형에 따른 라우팅")
    @Test
    void routePg() {
        // TOSS, NHN 모두 정상
        when(pgCacheService.isHealthy(PgProvider.TOSS))
                .thenReturn(true);
        when(pgCacheService.isHealthy(PgProvider.NHN))
                .thenReturn(true);

        assertThat(paymentPgRouter.routePg(PayMethod.CARD, PayProvider.KB))
                .as("KB 카드 결제는 TOSS만 존재")
                .isInstanceOf(TossStrategy.class);

        assertThat(paymentPgRouter.routePg(PayMethod.CARD, PayProvider.SAMSUNG))
                .as("SAMSUNG 카드 결제는 NHN이 수수료 낮아서 우선")
                .isInstanceOf(NHNStrategy.class);
    }

    @DisplayName("1위 장애시 2위 반환")
    @Test
    void routePg_health() {
        // NHN 장애, TOSS 정상
        when(pgCacheService.isHealthy(PgProvider.NHN)).thenReturn(false);
        when(pgCacheService.isHealthy(PgProvider.TOSS)).thenReturn(true);

        assertThat(paymentPgRouter.routePg(PayMethod.CARD, PayProvider.SAMSUNG))
                .as("SAMSUNG 카드 결제: NHN 장애로 TOSS로 폴백")
                .isInstanceOf(TossStrategy.class);
    }

    @DisplayName("ApplicationStartedEvent 동작 검증")
    @Test
    void initPgCache() {
        SpringApplication application = new SpringApplication(PlatformApplication.class);

        application.addListeners(
                (ApplicationListener<ApplicationReadyEvent>) event -> {
                    PaymentPgRouter targetBean = (PaymentPgRouter) event.getApplicationContext()
                            .getBean("paymentPgRouter");

//                    assertThat(targetBean.pgFeeCache).isNotEmpty();
                }
        );

        application.run();

    }
}