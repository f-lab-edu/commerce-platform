package com.commerce.platform.core.domain.vo;

import com.commerce.platform.shared.exception.BusinessException;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_QUANTITY;
import static com.commerce.platform.shared.exception.BusinessError.QUANTITY_EXCEEDS_MAXIMUM;

public record Quantity (
        long value
) {
    public Quantity add(Quantity quantity) {
        return new Quantity(this.value + quantity.value);
    }

    public Quantity minus(Quantity quantity) {
        if(this.value < quantity.value) throw new BusinessException(QUANTITY_EXCEEDS_MAXIMUM);

        return new Quantity(this.value - quantity.value);
    }

    public static Quantity create(long quantity) {
        return new Quantity(quantity);
    }

    public Quantity {
        if(value < 1) throw new BusinessException(INVALID_QUANTITY);
    }

}
