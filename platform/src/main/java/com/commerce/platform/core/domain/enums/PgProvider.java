package com.commerce.platform.core.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum PgProvider {
    TOSS,
    NHN,
    NICE_PAYMENTS,

    DANAL,
    PAYLETTER
    ;

    public static PgProvider getByPgName(String pgName) {
        return Arrays.stream(PgProvider.values())
                .filter(pg -> pg.name().equalsIgnoreCase(pgName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("미지원 PG사"));
    }

}
