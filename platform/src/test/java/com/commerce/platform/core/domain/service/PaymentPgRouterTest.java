package com.commerce.platform.core.domain.service;

import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.enums.PgProvider;
import com.commerce.platform.infrastructure.adaptor.PgCacheService;
import com.commerce.platform.infrastructure.pg.NHNStrategy;
import com.commerce.platform.infrastructure.pg.TossStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentPgRouterTest {
    @Autowired
    private PaymentPgRouter paymentPgRouter;

    @Autowired
    private PgCacheService pgCacheService;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @DisplayName("결제유형에 따른 라우팅")
    @Test
    void routePg() {
        assertThat(paymentPgRouter.routePg(PayMethod.CARD, PayProvider.KB))
                .as("KB 카드 결제는 TOSS 만 존재").isInstanceOf(TossStrategy.class);

        assertThat(paymentPgRouter.routePg(PayMethod.CARD, PayProvider.SAMSUNG))
                .as("SAMSUNG 카드 결제는 NHN 우선").isInstanceOf(NHNStrategy.class);

    }

    @DisplayName("1위 장애시 2위 반환")
    @Test
    void routePg_health() {
        // nhn 장애
        pgCacheService.markPgAsUnhealthy(PgProvider.NHN);

        assertThat(pgCacheService.isHealthy(PgProvider.NHN))
                .isEqualTo(false);

        assertThat(paymentPgRouter.routePg(PayMethod.CARD, PayProvider.SAMSUNG))
                .as("SAMSUNG 카드 결제 :  NHN 장애로 TOSS!").isInstanceOf(TossStrategy.class);

        // nhn 장애 원복
        redisTemplate.delete("pg:health:NHN");
    }
}