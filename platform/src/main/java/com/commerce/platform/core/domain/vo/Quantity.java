package com.commerce.platform.core.domain.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Quantity {
    private long value;

    public Quantity add(Quantity quantity) throws Exception {
        if(quantity.getValue() <= 0) throw new Exception("수량은 1 이상");

        return new Quantity(this.value + quantity.value);
    }

    public Quantity minus(Quantity quantity) throws Exception {
        if(quantity.getValue() < 1) throw new Exception("소진수량 확인 요망");
        if(this.value < quantity.value) throw new Exception("수량 부족");

        return new Quantity(this.value - quantity.value);
    }

    public void checkQuantity() throws Exception {
        if(this.value < 1) throw new Exception("수량 확인");
    }

    public static Quantity create(long quantity) {
        return Quantity.builder()
                .value(quantity)
                .build();
    }
}
