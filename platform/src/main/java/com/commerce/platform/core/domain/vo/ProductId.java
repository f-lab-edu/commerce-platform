package com.commerce.platform.core.domain.vo;

import com.commerce.platform.shared.exception.BusinessException;
import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_PRODUCT_ID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class ProductId {
    private String id;

    public static ProductId of(String id) {
        if(StringUtils.isBlank(id)
            || id.charAt(0) != 'P') throw new BusinessException(INVALID_PRODUCT_ID);
        return ProductId.builder()
                .id(id)
                .build();
    }

    public static ProductId create() {return ProductId();}

    private static ProductId ProductId() {
        return ProductId.builder()
                .id("P" + LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS"))
                ).build();
    }
}
