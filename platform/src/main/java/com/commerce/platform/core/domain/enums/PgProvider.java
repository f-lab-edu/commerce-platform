package com.commerce.platform.core.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum PgProvider {
    TOSS(List.of(PayMethod.CARD, PayMethod.EASY_PAY)),
    NHN(List.of(PayMethod.CARD, PayMethod.EASY_PAY)),
    NICE_PAYMENTS(List.of(PayMethod.CARD, PayMethod.EASY_PAY)),

    DANAL(List.of(PayMethod.PHONE)),
    PAYLETTER(List.of(PayMethod.PHONE))
    ;

    private final List<PayMethod> payMethods;

    public static List<PgProvider> getByPayMethod(PayMethod payMethod) {
        List<PgProvider> pgProviders = Arrays.stream(PgProvider.values())
                .filter(pg -> pg.getPayMethods().contains(payMethod))
                .toList();

        if(pgProviders.isEmpty()) throw new IllegalArgumentException("지원 PG사 없음");
        return pgProviders;
    }

}
