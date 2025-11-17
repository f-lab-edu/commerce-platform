package com.commerce.platform.infrastructure.pg;

import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.domain.enums.PgProvider;
import org.springframework.stereotype.Component;

@Component
public class TossStrategy extends PgStrategy {

    @Override
    public PgProvider getPgProvider() {
        return PgProvider.TOSS;
    }
}
