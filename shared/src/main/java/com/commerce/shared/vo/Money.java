package com.commerce.shared.vo;

import com.commerce.shared.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import static com.commerce.shared.exception.BusinessError.INVALID_MONEY;

@Embeddable
public record Money(
        @Column(name = "value")
        long value
) {
    public Money add(Money money) {
        return of(this.value + money.value);
    }

    public Money subtract(Money money) {
        return of(this.value - money.value);
    }

    public Money discount(int percent) {
        return of(this.value * (100 - percent));
    }

    public Money multiply(Quantity quantity) {
        return of(this.value * quantity.value());
    }

    public boolean isGreaterThan(Money other) {
        return this.value > other.value;
    }

    public static Money of(long value) {
        return new Money(value);
    }

    // compact constructor
    public Money {
        if(value < 0) {
            throw new BusinessException(INVALID_MONEY);
        }
    }

}
