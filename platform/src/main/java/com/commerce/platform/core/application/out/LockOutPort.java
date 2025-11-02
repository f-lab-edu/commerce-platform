package com.commerce.platform.core.application.out;

public interface LockOutPort {
    Integer getLock(String lockName, int timeoutSeconds);
    Integer releaseLock(String lockName);
}
