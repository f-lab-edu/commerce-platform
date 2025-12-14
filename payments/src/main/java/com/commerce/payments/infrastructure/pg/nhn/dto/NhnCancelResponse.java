package com.commerce.payments.infrastructure.pg.nhn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NhnCancelResponse(
        // 결과 정상 승인: "0000", 그 외 오류코드
        @JsonProperty("res_cd")
        String resCd,

        // 결과 메시지
        @JsonProperty("res_msg")
        String resMsg,

        // KCP 거래 고유번호 (14자리)
        @JsonProperty("tno")
        String tno,

        // 취소 처리 시각 yyyyMMddHHmmss
        @JsonProperty("canc_time")
        String cancTime,

        // 부분취소 금액 전체 취소 시: null 또는 "0"
        @JsonProperty("mod_mny")
        String modMny,

        // 부분취소 후 남은 금액
        @JsonProperty("rem_mny")
        String remMny,

        // 부분취소 일련번호
        @JsonProperty("mod_pcan_seq_no")
        String modPcanSeqNo,

        // 카드 취소금액
        @JsonProperty("card_mod_mny")
        String cardModMny,

        // 쿠폰 취소금액
        @JsonProperty("coupon_mod_mny")
        String couponModMny
) {
}
