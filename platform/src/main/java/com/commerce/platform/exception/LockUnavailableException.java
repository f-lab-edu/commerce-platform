package com.commerce.platform.exception;

public class LockUnavailableException extends RuntimeException{
    public LockUnavailableException(String message) {
        super(message);
    }
}
