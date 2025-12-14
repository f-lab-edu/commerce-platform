package com.commerce.payments.infrastructure.pg.nhn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * NHN 제휴간편결제 신용카드 결제응답
 */
public record NhnEasyCardApprovalResponse(
        @JsonProperty("res_cd")
        String resCd, // 결과코드 (정상: "0000")

        @JsonProperty("res_msg")
        String resMsg, // 결과 메시지

        @JsonProperty("res_en_msg")
        String resEnMsg, // 영문 결과 메시지

        @JsonProperty("amount")
        String amount,  // 총 결제금액 (DB 금액 검증 필수)

        @JsonProperty("tno")
        String tno,     // KCP 거래번호 (14자리, 전체 사용 필수

        @JsonProperty("card_other_pay_type")
        String cardOtherPayType // 제휴간편결제유형
) implements NhnApprovalResponse{
}
