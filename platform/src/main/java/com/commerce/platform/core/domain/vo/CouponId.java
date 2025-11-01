package com.commerce.platform.core.domain.vo;

import com.commerce.platform.shared.exception.BusinessException;
import io.micrometer.common.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_COUPON;

public record CouponId(
        String id
) {
    public static CouponId create() {
        return new CouponId("C" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")));
    }

    public static CouponId of(String couponId) {
        if(couponId == null) return null;
        return new CouponId(couponId);
    }

    public CouponId {
        if(!StringUtils.isBlank(id)
                && id.charAt(0) != 'C') throw new BusinessException(INVALID_COUPON);
    }
}
