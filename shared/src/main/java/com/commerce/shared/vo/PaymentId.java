package com.commerce.shared.vo;

import com.commerce.shared.exception.BusinessException;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.commerce.shared.exception.BusinessError.INVALID_PAYMENT;

@Embeddable
public record PaymentId(
        @Column(name = "id", length = 21)
        String id
) implements Serializable {

    public static PaymentId create() {
        return new PaymentId("T" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")));
    }

    public PaymentId {
        if(StringUtils.isBlank(id)
                || id.charAt(0) != 'T') throw new BusinessException(INVALID_PAYMENT);
    }
}
