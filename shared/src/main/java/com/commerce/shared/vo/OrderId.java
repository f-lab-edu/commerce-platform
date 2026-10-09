package com.commerce.shared.vo;

import com.commerce.shared.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

import static com.commerce.shared.exception.BusinessError.INVALID_ORDER_ID;

@Embeddable
public record OrderId (
        @Column(name = "id", length = OrderId.LENGTH)
        String id
) implements Serializable {

    @JsonCreator
    public static OrderId of(String id) {
        if(StringUtils.isBlank(id)
                || id.charAt(0) != 'O') throw new BusinessException(INVALID_ORDER_ID);
        return new OrderId(id);
    }

    /** "O" + UUID(36) */
    public static final int LENGTH = 37;

    /**
     * 시각 기반 ID는 동시 생성 시 충돌할 수 있어 UUID를 쓴다.
     * orderId는 파티션 키이자 PG 멱등 키라 충돌하면 다른 주문의 결제 결과가 반환될 수 있다.
     */
    public static OrderId create() {
        return new OrderId("O" + UUID.randomUUID());
    }

    public OrderId {
        if(StringUtils.isBlank(id)
                || id.charAt(0) != 'O') throw new BusinessException(INVALID_ORDER_ID);
    }

    @JsonValue
    public String getId() {
        return id;
    }
}

