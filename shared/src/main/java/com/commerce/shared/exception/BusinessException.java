package com.commerce.shared.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private String code;
    private String message;

    public BusinessException(BusinessError de) {
        super(de.getMessage());
        this.code = de.getCode();
        this.message = de.getMessage();
    }
}