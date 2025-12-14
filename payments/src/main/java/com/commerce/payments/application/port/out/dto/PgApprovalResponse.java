package com.commerce.payments.application.port.out.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PgApprovalResponse {
    private final String pgTid;
    private final Long approvedAmount;
    private final LocalDateTime approvedAt;
}
