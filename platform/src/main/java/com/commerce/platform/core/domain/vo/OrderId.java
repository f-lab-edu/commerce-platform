package com.commerce.platform.core.domain.vo;

import com.commerce.platform.shared.exception.BusinessException;
import io.micrometer.common.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_ORDER_ID;

public record OrderId (
        String id
){
    public static OrderId of(String id) {
        if(StringUtils.isBlank(id)
                || id.charAt(0) != 'P') throw new BusinessException(INVALID_ORDER_ID);
        return new OrderId(id);
    }

    public static OrderId create() {
        return new OrderId("O" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")));
    }

    public OrderId {
        if(StringUtils.isBlank(id)
                || id.charAt(0) != 'O') throw new BusinessException(INVALID_ORDER_ID);
    }
}
