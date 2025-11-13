package com.commerce.platform.core.domain.service;

import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.infrastructure.pg.NHNStrategy;
import com.commerce.platform.infrastructure.pg.TossStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentPgRouterTest {
    @Autowired
    private PaymentPgRouter paymentPgRouter;

    @DisplayName("결제유형에 따른 라우팅")
    @Test
    void routPg() {
        PgStrategy pgStrategy = paymentPgRouter.routPg(PayMethod.CARD, PayProvider.KB);

        assertThat(pgStrategy)
                .as("카드결제는 토스, NHN 지원").isInstanceOfAny(TossStrategy.class, NHNStrategy.class);

    }
}