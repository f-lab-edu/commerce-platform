package com.commerce.platform.core.domain.vo;

import com.commerce.platform.shared.exception.BusinessException;
import io.micrometer.common.util.StringUtils;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_CUSTOMER;

public record CustomerId (
        String id
) {
    public static CustomerId of(String customerId) {
        return new CustomerId(customerId);
    }

    public CustomerId {
        validate();
    }

    private void validate() {
        if(StringUtils.isBlank(id)) throw new BusinessException(INVALID_CUSTOMER);
    }
}
