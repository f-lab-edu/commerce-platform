package com.commerce.platform.core.domain.vo;

import com.commerce.platform.shared.exception.BusinessException;

import java.time.LocalDate;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_PERIOD;

public record ValidPeriod (
        LocalDate frDt,
        LocalDate toDt
) {
    public static ValidPeriod create(LocalDate frDt, LocalDate toDt) {
        return new ValidPeriod(frDt, toDt);
    }

    public ValidPeriod {
        validate();
    }

    private void validate() {
        if(toDt.isBefore(frDt)) throw new BusinessException(INVALID_PERIOD);
    }
}
