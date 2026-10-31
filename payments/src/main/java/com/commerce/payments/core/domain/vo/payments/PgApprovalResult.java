package com.commerce.payments.core.domain.vo.payments;

import com.commerce.payments.core.domain.enums.PgApprovalStatus;
import com.commerce.payments.core.domain.enums.PgProvider;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * PG 승인 처리 결과 (필요 시 망취소까지 반영된 최종 결과)
 */
@Builder
public record PgApprovalResult(
        PgApprovalStatus status,
        PgProvider pgProvider,
        String pgTid,                 // pg 승인Tid
        String pgCancelTid,           // pg 망취소Tid
        Long pgApprovedAmt,           // pg 승인금액
        String responseCode,          // pg사 응답코드
        String reason,                // 실패사유 (승인성공 시 null)
        LocalDateTime approvedAt,
        LocalDateTime canceledAt
) {
    public boolean isApproved() {
        return status == PgApprovalStatus.APPROVED;
    }
}
