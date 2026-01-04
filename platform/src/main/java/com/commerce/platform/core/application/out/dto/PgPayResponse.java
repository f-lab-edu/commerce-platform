package com.commerce.platform.core.application.out.dto;

import com.commerce.platform.core.domain.vo.Money;
import lombok.Builder;

/**
 * pg서 승인 응답 결과 DTO
 * 결제유형에 따라 card/easyPay/virtualAccount 필드 세팅된다.
 */
@Builder
public record PgPayResponse (
        String pgTid,
        String responseCode,          // pg사 응답코드
        String responseMessage,       // pg사 응답메시지
        Money amount,
        boolean isSuccess,
        Card card,                    // 카드결제 응답
        EasyPay easyPay,              // 간편결제 응답
        VirtualAccount virtualAccount // 가상계좌 응답
) {
    @Builder
    public record Card(
            String approveNo,
            String issuerCode,
            String cardType
    ) {}

    @Builder
    public record EasyPay(
            String provider,
            String amount,
            String discountAmount
    ) {}

    @Builder
    public record VirtualAccount(
            String accountType,
            String accountNumber,
            String bankCode,
            String depositorName,
            String dueDate
    ) {}
}
