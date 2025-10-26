package com.commerce.platform.bootstrap.exception;

public record ErrorResponse (
        String path,
        String method,
        String code,
        String message
){ }
