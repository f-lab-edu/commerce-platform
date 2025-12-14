package com.commerce.payments;

import lombok.Builder;

/**
 * pg서 취소 응답 결과 DTO
 */
@Builder
public record PgPayCancelResponse(
        String pgCcTid,
        String responseCode,          // pg사 응답코드
        String responseMessage,       // pg사 응답메시지
        String cancelReason,          // 취소사유
        Long   cancelAmount,          // 취소 금액
        boolean isSuccess
) { }
