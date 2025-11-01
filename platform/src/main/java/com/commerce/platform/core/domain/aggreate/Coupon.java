package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.Quantity;
import com.commerce.platform.core.domain.vo.ValidPeriod;
import com.commerce.platform.shared.exception.BusinessException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.commerce.platform.shared.exception.BusinessError.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Coupon {
    private CouponId couponId;
    private String couponName;
    private String code;
    private int discountPercent;
    private Money minOrderAmt;
    private Money maxDiscountAmt;
    private ValidPeriod validPeriod;
    private Quantity totalQuantity;
    private Quantity issuedQuantity;
    private LocalDateTime createdAt;

    public static Coupon create(
            String code,
            String couponName,
            int discountPercent,
            int minOrderAmt,
            int maxDiscountAmt,
            LocalDate frDt,
            LocalDate toDt,
            long totalQuantity
    ) throws Exception {

        return Coupon.builder()
                .couponId(CouponId.create())
                .couponName(couponName)
                .code(code)
                .discountPercent(discountPercent)
                .minOrderAmt(Money.create(minOrderAmt))
                .maxDiscountAmt(Money.create(maxDiscountAmt))
                .validPeriod(ValidPeriod.create(frDt, toDt))
                .totalQuantity(Quantity.create(totalQuantity))
                .issuedQuantity(Quantity.create(0))
                .build();
    }

    /**
     * 사용가능여부 확인
     */
    public void isAvailable(Money orderAmt) {
        // 주문금액 확인
        if(this.minOrderAmt.value() > orderAmt.value()) throw new BusinessException(BELOW_LEAST_ORDER_AMT);

        // 유효기간 확인
        isValidPeriod();
    }

    /**
     * 발급
     */
    public void issueCoupon() {
        isValidPeriod();

        // 수량 확인
        try {
            this.totalQuantity.minus(this.issuedQuantity.add(Quantity.create(1)));
        } catch (BusinessException be) {
            throw new BusinessException(QUANTITY_EXCEEDED_COUPON);
        }

        // 발급수량 ++
        this.issuedQuantity = Quantity.create(this.issuedQuantity.value() + 1L);
    }

    /**
     * 할인금액 계산
     */
    public Money calculateDiscountAmt(Money orderAmt) {
        isAvailable(orderAmt);

        // 할인금액
        Money discountAmt = orderAmt.discount(this.discountPercent);
        if(discountAmt.value() > this.maxDiscountAmt.value()) {
            return this.maxDiscountAmt;
        }

        return discountAmt;
    }

    private void isValidPeriod() {
        if(!validPeriod.nowInPeriod()) throw new BusinessException(NOT_WITHIN_PERIOD_COUPON);
    }
}
