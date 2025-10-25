package com.commerce.platform.core.domain.vo;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Money {
    private long value;

    public Money add(Money money) {
        return create(this.value + money.value);
    }

    public Money substract(Money money) {
        return create(this.value - money.value);
    }

    public Money discount(int percent) {
        return create(this.value * (100 - percent));
    }

    public Money multiply(Quantity quantity) {
        return create(this.value * quantity.getValue());
    }

    public static Money create(long value) {
        return Money.builder()
                .value(value)
                .build();
    }

}
