package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.enums.PgProvider;

public abstract class PgStrategy {

    public abstract PgProvider getPgProvider();

}
