package com.commerce.shared.vo;

import com.commerce.shared.exception.BusinessError;
import com.commerce.shared.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Quantity (
        @Column(name = "value")
        long value
) {
    public Quantity add(Quantity quantity) {
        return new Quantity(this.value + quantity.value);
    }

    public Quantity minus(Quantity quantity) {
        if(this.value < quantity.value) throw new BusinessException(BusinessError.QUANTITY_EXCEEDS_MAXIMUM);

        return new Quantity(this.value - quantity.value);
    }

    public static Quantity create(long quantity) {
        return new Quantity(quantity);
    }

    public Quantity {
        if(value < 0) throw new BusinessException(BusinessError.INVALID_QUANTITY);
    }

}
