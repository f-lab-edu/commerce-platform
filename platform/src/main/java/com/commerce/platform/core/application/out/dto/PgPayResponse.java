package com.commerce.platform.core.application.out.dto;

public record PgPayResponse (
        String pgTid,
        String responseCode,    // pg사 응답코드
        String responseMessage, // pg사 응답메시지
        boolean isSuccess
) {}
