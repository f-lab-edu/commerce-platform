package com.commerce.platform.shared.exception;

public class LockUnavailableException extends RuntimeException{
    public LockUnavailableException(String message) {
        super(message);
    }
}
