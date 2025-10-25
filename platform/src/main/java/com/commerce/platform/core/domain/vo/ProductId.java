package com.commerce.platform.core.domain.vo;

import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class ProductId {
    private String id;

    public static ProductId of(String id) {
        if(StringUtils.isBlank(id)
            || id.charAt(0) != 'P') throw new RuntimeException("상품ID가 아닙니다.");
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
