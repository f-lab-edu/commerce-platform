package com.commerce.payments.infrastructure.pg.nhn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * NHN 카드결제 승인응답
 */
public record NhnCardApprovalResponse(
        @JsonProperty("res_cd")
        String resCd, // 결과코드 (정상: "0000")

        @JsonProperty("res_msg")
        String resMsg, // 결과 메시지

        @JsonProperty("res_en_msg")
        String resEnMsg, // 영문 결과 메시지

        @JsonProperty("pay_method")
        String payMethod, // 결제수단 (PACA:카드, PABK:계좌이체, PAMC:휴대폰 등)

        @JsonProperty("order_no")
        String orderNo,

        @JsonProperty("amount")
        String amount,  // 총 결제금액 (DB 금액 검증 필수)

        @JsonProperty("card_mny")
        String cardMny, // 카드 실결제금액 (쿠폰제외, 100%할인시 0 가능)

        @JsonProperty("coupon_mny")
        String couponMny, // 쿠폰/포인트 할인금액

        @JsonProperty("card_no")
        String cardNo,  // 카드번호 (3번째 구간 마스킹)

        @JsonProperty("card_bin_type_02")
        String cardBinType02, //  일반 : 0 / 체크 : 1

        @JsonProperty("quota")
        String quota,   // 할부개월 (00:일시불, 03:3개월)

        @JsonProperty("tno")
        String tno,     // KCP 거래번호 (14자리, 전체 사용 필수)

        @JsonProperty("card_cd")
        String cardCd,  // 카드사 코드 (예: CCNH)

        @JsonProperty("card_name")
        String cardName, // 카드사 명 (예: NH카드)

        @JsonProperty("app_no")
        String appNo    // 승인번호 (8자리)
) implements NhnApprovalResponse{
}
