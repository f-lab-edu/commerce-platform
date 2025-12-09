package com.commerce.platform.infrastructure.pg.nhn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * NHN 결제 승인 요청
 */
@Builder
public record NhnApprovalRequest(
        @JsonProperty("site_cd")
        String siteCd,  // siteCd 사이트코드 (5자리, 영문대문자+숫자)

        @JsonProperty("kcp_cert_info")
        String kcpCertInfo, // NHN KCP 서비스 인증서 (PEM 파일 직렬화)

        @JsonProperty("enc_data")
        String encData, // 결제창 인증결과 암호화 정보 (결제창에서 받은 값 그대로 사용)

        @JsonProperty("enc_info")
        String encInfo, // 결제창 인증결과 암호화 정보 (결제창에서 받은 값 그대로 사용)

        @JsonProperty("tran_cd")
        String tranCd,  // 요청코드 (고정값: "00100000")

        @JsonProperty("ordr_mony")
        String ordrMony, // 실제 결제 요청 금액

        @JsonProperty("ordr_no")
        String ordrNo,  // 실제 결제 주문번호

        @JsonProperty("pay_type")
        String payType  // 결제수단 구분 신용카드 : PACA
) { }
