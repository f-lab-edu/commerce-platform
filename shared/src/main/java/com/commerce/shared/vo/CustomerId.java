package com.commerce.shared.vo;

import com.commerce.shared.exception.BusinessException;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

import static com.commerce.shared.exception.BusinessError.INVALID_CUSTOMER;

@Embeddable
public record CustomerId (
        @Column(name = "id", length = 21)
        String id
) implements Serializable  {
    public static CustomerId of(String customerId) {
        return new CustomerId(customerId);
    }

    public CustomerId {
        if(StringUtils.isBlank(id)) throw new BusinessException(INVALID_CUSTOMER);
    }

}
