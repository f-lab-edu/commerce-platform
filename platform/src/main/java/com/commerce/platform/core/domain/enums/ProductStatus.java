package com.commerce.platform.core.domain.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ProductStatus {
    ACTIVE("활성"),
    INACTIVE("비활성"),
    OUT_OF_STOCK("품절"),
    DISCONTINUED("단종");

    private final String value;
}
