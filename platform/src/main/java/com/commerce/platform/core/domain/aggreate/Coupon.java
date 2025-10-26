package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.Quantity;
import com.commerce.platform.core.domain.vo.ValidPeriod;

import java.time.LocalDate;
import java.util.UUID;

public class Coupon {
    private String couponId;
    private String code;
    private int discountPercent;
    private Money minOrderAmt;
    private Money maxDiscountAmt;
    private ValidPeriod validPeriod;
    private Quantity totalQuantity;
    private Quantity remainQuantity;

    public static Coupon create(
            String code,
            int discountPercent,
            int minOrderAmt,
            int maxDiscountAmt,
            LocalDate frDt,
            LocalDate toDt,
            int totalCnt
    ) throws Exception {
        Coupon coupon = new Coupon();
        coupon.couponId = String.valueOf(UUID.randomUUID());
        coupon.code = code;
        coupon.discountPercent = discountPercent;
        coupon.minOrderAmt = Money.create(minOrderAmt);
        coupon.maxDiscountAmt = Money.create(maxDiscountAmt);
        coupon.validPeriod = ValidPeriod.create(frDt, toDt);
        coupon.totalQuantity = Quantity.create(totalCnt);
        coupon.remainQuantity = coupon.totalQuantity;

        return coupon;
    }

    /**
     * 쿠폰 적용 가능여부 확인
     */
    public boolean valid(Money orerAmt) {
        try {
            // 쿠폰 수량 확인
            this.remainQuantity.minus(Quantity.create(1));

            // 주문금액 확인
            if(this.minOrderAmt.value() > orerAmt.value()) throw new Exception("최소주문금액 미달");

            // 유효기간 확인
//            if(!this.validPeriod.checkNowInPeriod()) throw new Exception("적용가능일자가 아님");

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 쿠폰적용
     */
    public Money useCoupon(Money orderAmt) throws Exception {
        // 수량--
        this.remainQuantity = this.remainQuantity.minus(Quantity.create(1));

        // 할인금액
        Money discountAmt = orderAmt.discount(this.discountPercent);
        if(discountAmt.value() > this.maxDiscountAmt.value()) {
            discountAmt = this.maxDiscountAmt;
        }

        return discountAmt;
    }
}
