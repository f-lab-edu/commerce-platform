package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.enums.PgProvider;

import java.util.Map;

public abstract class PgStrategy {

    public final Map<String, String> processApproval() {
        // todo pg별 전후처리
        return doPay();
    }

    public final Map<String, String> processFullCancel() {
        // todo pg별 전후처리
        return doCancel();
    }

    public final Map<String, String> processPartCancel() {
        // todo pg별 전후처리
        return doPartCancel();
    }

    public abstract PgProvider getPgProvider();

    // todo 임시로 pg사 응답 타입 map
    protected abstract Map<String, String> doPay();
    protected abstract Map<String, String> doCancel();
    protected abstract Map<String, String> doPartCancel();
}
