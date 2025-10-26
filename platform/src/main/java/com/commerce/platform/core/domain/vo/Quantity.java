package com.commerce.platform.core.domain.vo;

import com.commerce.platform.shared.exception.BusinessException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_QUANTITY;
import static com.commerce.platform.shared.exception.BusinessError.QUANTITY_EXCEEDS_MAXIMUM;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Quantity {
    private long value;

    public Quantity add(Quantity quantity) {
        checkQuantity();
        return new Quantity(this.value + quantity.value);
    }

    public Quantity minus(Quantity quantity) {
        checkQuantity();
        if(this.value < quantity.value) throw new BusinessException(QUANTITY_EXCEEDS_MAXIMUM);

        return new Quantity(this.value - quantity.value);
    }

    public void checkQuantity() {
        if(this.value < 1) throw new BusinessException(INVALID_QUANTITY);
    }

    public static Quantity create(long quantity) {
        return Quantity.builder()
                .value(quantity)
                .build();
    }
}
