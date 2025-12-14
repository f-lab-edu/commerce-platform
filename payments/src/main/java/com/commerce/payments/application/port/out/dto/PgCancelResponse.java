package com.commerce.payments.application.port.out.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PgCancelResponse {
    private final String pgTid;
    private final Long canceledAmount;
    private final LocalDateTime canceledAt;
}
