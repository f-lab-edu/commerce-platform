package com.commerce.platform.core.domain.enums;

import io.micrometer.common.util.StringUtils;

public enum StockOperation {
    SET("set"),
    INCREASE("increase"),
    DECREASE("decrease");

    private final String value;

    StockOperation(String value) {
        if(StringUtils.isBlank(value)) throw new IllegalStateException("재고 명령 확인 요망");
        this.value = value;
    }
}
