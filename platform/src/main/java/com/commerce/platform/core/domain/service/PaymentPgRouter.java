package com.commerce.platform.core.domain.service;

import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PgProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentPgRouter {
    private final Map<PgProvider, PgStrategy> pgStrategies;

    public PaymentPgRouter(List<PgStrategy> list) {
        this.pgStrategies = list.stream()
                .collect(Collectors.toMap(PgStrategy::getPgProvider, pg -> pg));
    }

    public PgStrategy routPg(PayMethod payMethod) {
        List<PgProvider> availablePgList = PgProvider.getByPayMethod(payMethod);

        // todo 조건에 따른 pg사 선택
        PgProvider targetPg = availablePgList.get(0);

        return pgStrategies.get(targetPg);
    }

}
