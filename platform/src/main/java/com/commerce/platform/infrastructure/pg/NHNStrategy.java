package com.commerce.platform.infrastructure.pg;

import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.domain.enums.PgProvider;
import org.springframework.stereotype.Component;

@Component
public class NHNStrategy extends PgStrategy {

    @Override
    public String process(String request) {
        return "";
    }

    public PgProvider getPgProvider() {
        return PgProvider.NHN;
    }
}
