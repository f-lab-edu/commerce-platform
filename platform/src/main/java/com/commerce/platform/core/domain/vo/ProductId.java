package com.commerce.platform.core.domain.vo;

import com.commerce.shared.exception.BusinessException;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.commerce.shared.exception.BusinessError.INVALID_PRODUCT_ID;

@Embeddable
public record ProductId (
        @Column(name = "id", length = 21)
        String id
) implements Serializable {
    public static ProductId of(String id) {
        return new ProductId(id);
    }

    public static ProductId create() {
        return new ProductId("P" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")));
    }

    public ProductId {
        if(StringUtils.isBlank(id)
                || id.charAt(0) != 'P') throw new BusinessException(INVALID_PRODUCT_ID);
    }
}
