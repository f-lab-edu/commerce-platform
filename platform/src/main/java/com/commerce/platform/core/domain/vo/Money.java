package com.commerce.platform.core.domain.vo;

import com.commerce.platform.shared.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_MONEY;

@Embeddable
public record Money(
        @Column(name = "value")
        long value
) {
    public Money add(Money money) {
        return create(this.value + money.value);
    }

    public Money subtract(Money money) {
        return create(this.value - money.value);
    }

    public Money discount(int percent) {
        return create(this.value * (100 - percent));
    }

    public Money multiply(Quantity quantity) {
        return create(this.value * quantity.value());
    }

    public static Money create(long value) {
        return new Money(value);
    }

    // compact constructor
    public Money {
        if(value < 0) {
            throw new BusinessException(INVALID_MONEY);
        }
    }
}
