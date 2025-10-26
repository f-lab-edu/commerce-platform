package com.commerce.platform.core.domain.vo;

import com.commerce.platform.shared.exception.BusinessException;
import io.micrometer.common.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_PRODUCT_ID;

public record ProductId (
        String id
) {
    public static ProductId of(String id) {
        return new ProductId(id);
    }

    public static ProductId create() {
        return new ProductId("P" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")));
    }

    public ProductId {
        validate();
    }

    private void validate() {
        if(StringUtils.isBlank(id)
                || id.charAt(0) != 'P') throw new BusinessException(INVALID_PRODUCT_ID);
    }

}
