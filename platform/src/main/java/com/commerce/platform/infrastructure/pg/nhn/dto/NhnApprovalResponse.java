package com.commerce.platform.infrastructure.pg.nhn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * NHN 승인요청
 */
public record NhnApprovalResponse(
        @JsonProperty("res_cd")
                String resCd, // 결과코드 (정상: "0000")

        @JsonProperty("res_msg")
        String resMsg, // 결과 메시지

        @JsonProperty("res_en_msg")
        String resEnMsg, // 영문 결과 메시지

        @JsonProperty("pay_method")
        String payMethod, // 결제수단 (PACA:카드, PABK:계좌이체, PAMC:휴대폰 등)

        @JsonProperty("tno")
        String tno, // KCP 거래번호 (14자리, 전체 사용 필수)

        @JsonProperty("amount")
        String amount, // 총 결제금액 (DB 금액 검증 필수)

        /** === 신용카드 전용 필드 === */
        @JsonProperty("card_cd")
        String cardCd, // 카드사 코드 (예: CCNH)

        @JsonProperty("card_name")
        String cardName, // 카드사 명 (예: NH카드)

        @JsonProperty("card_no")
        String cardNo, // 카드번호 (3번째 구간 마스킹)

        @JsonProperty("app_no")
        String appNo, // 승인번호 (8자리)

        @JsonProperty("app_time")
        String appTime, // 승인시간 (yyyyMMddHHmmss)

        @JsonProperty("noinf")
        String noinf, // 무이자 여부 (Y/N)

        @JsonProperty("noinf_type")
        String noinfType, // 무이자 유형 (CARD:카드사, SHOP:상점부담)

        @JsonProperty("quota")
        String quota, // 할부개월 (00:일시불, 03:3개월)

        @JsonProperty("card_mny")
        String cardMny, // 카드 실결제금액 (쿠폰제외, 100%할인시 0 가능)

        @JsonProperty("coupon_mny")
        String couponMny, // 쿠폰/포인트 할인금액

        @JsonProperty("payco_point_mny")
        String paycoPointMny, // 페이코 포인트 사용금액

        @JsonProperty("partcanc_yn")
        String partcancYn, // 부분취소 가능여부 (Y/N)

        @JsonProperty("card_bin_type_01")
        String cardBinType01, // 카드 소유자 (0:개인, 1:법인)

        @JsonProperty("card_bin_type_02")
        String cardBinType02, // 카드 유형 (0:신용, 1:체크)

        @JsonProperty("isp_issuer_cd")
        String ispIssuerCd, // ISP 발급사코드 (BC96:케이뱅크, KM90:카카오뱅크)

        @JsonProperty("isp_issuer_nm")
        String ispIssuerNm // ISP 발급사명
) {
}
