package com.commerce.platform.core.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum PgProvider {
    TOSS(
            Set.of(PayMethod.CARD, PayMethod.EASY_PAY),
            Set.of(PayProvider.SHIN_HAN, PayProvider.KB, PayProvider.NH,
                    PayProvider.HYUNDAI, PayProvider.SAMSUNG, PayProvider.BC)
    ),
    NHN(
            Set.of(PayMethod.CARD, PayMethod.EASY_PAY),
            Set.of(PayProvider.NH, PayProvider.HYUNDAI,
                    PayProvider.SAMSUNG, PayProvider.BC)
    ),
    NICE_PAYMENTS(
            Set.of(PayMethod.CARD, PayMethod.EASY_PAY),
            Set.of(PayProvider.HANA, PayProvider.LOTTE,
                    PayProvider.SAMSUNG, PayProvider.BC)
    ),

    DANAL(
            Set.of(PayMethod.PHONE),
            Set.of(PayProvider.LG, PayProvider.KT, PayProvider.SKT)

    ),
    PAYLETTER(
            Set.of(PayMethod.PHONE),
            Set.of(PayProvider.LG, PayProvider.KT)
    )
    ;

    private final Set<PayMethod> payMethods;
    private final Set<PayProvider> payProviders;

    public static List<PgProvider> getByPayMethod(PayMethod payMethod, PayProvider payProvider) {
        List<PgProvider> pgProviders = Arrays.stream(PgProvider.values())
                .filter(pg -> pg.getPayMethods().contains(payMethod))
                .filter(pg -> pg.getPayProviders().contains(payProvider))
                .toList();

        if(pgProviders.isEmpty()) throw new IllegalArgumentException("지원 PG사 없음");
        return pgProviders;
    }

}
