package com.commerce.platform.core.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Money {
    private long value;

    public Money add(Money money) {
        return new Money(this.value + money.value);
    }

    public Money substract(Money money) {
        return new Money(this.value - money.value);
    }

    public Money discount(int percent) {
        return new Money(this.value * (100 - percent));
    }

    public Money multiply(Quantity quantity) {
        return new Money(this.value * quantity.getValue());
    }

}
