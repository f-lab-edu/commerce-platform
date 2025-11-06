package com.commerce.platform.core.domain.vo;

import com.commerce.platform.shared.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_PERIOD;

@Embeddable
public record ValidPeriod (
        @Column(name = "fr_dt", nullable = false)
        LocalDate frDt,

        @Column(name = "to_dt", nullable = false)
        LocalDate toDt
) implements Serializable {
    public static ValidPeriod create(LocalDate frDt, LocalDate toDt) {
        return new ValidPeriod(frDt, toDt);
    }

    public ValidPeriod {
        if(toDt.isBefore(frDt)) throw new BusinessException(INVALID_PERIOD);
    }
    
    public boolean nowInPeriod() {
        if(!LocalDate.now().isBefore(frDt)
                && !LocalDate.now().isAfter(toDt)) {
            return true;
        }
        return false;
    }
}
