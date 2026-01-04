package com.commerce.platform.core.domain.service;

import com.commerce.platform.core.application.port.out.LockOutPort;
import com.commerce.platform.exception.LockUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Log4j2
@RequiredArgsConstructor
@Service
public class LockFacade {
    private final LockOutPort lockOutPort;
    private final TransactionTemplate transactionTemplate;

    private static final int LOCK_TIMEOUT_SECONDS = 3;
    private static final int MAX_RETRY_COUNT = 5;

    public <T> T executeWithLock(String prefix, String target, Supplier<T> supplier) {
        String lockName = prefix + target;
        int retryCnt = 0;

        while (retryCnt <= MAX_RETRY_COUNT) {
            try {
                return tryExecuteWithLock(lockName, supplier);
            } catch (LockUnavailableException e){
                retryCnt++;
                if(retryCnt > MAX_RETRY_COUNT) throw new LockUnavailableException("재시도 횟수 초과");

                try {
                    log.info("Thread : {}, lock name : {}, retryCnt : {}", Thread.currentThread().getName(), lockName, retryCnt);

                    Thread.currentThread().sleep(5 * 1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("interrupt 발생");
                }
            }
        }

        throw new LockUnavailableException("lock 획득 실패");
    }

    /**
     * 락 획득하면 실행
     * @param lockName
     * @param supplier
     * @param <T>
     * @return
     */
    private <T> T tryExecuteWithLock(String lockName, Supplier<T> supplier) {
        boolean lockAcquired = false;
        try {
            Integer lock = lockOutPort.getLock(lockName, LOCK_TIMEOUT_SECONDS);
            if(lock != 1) throw new LockUnavailableException("todo 잠시후 재시도하세요");

            lockAcquired = true;
            return transactionTemplate.execute(status -> supplier.get());

        } finally {
            if(lockAcquired) lockOutPort.releaseLock(lockName);
        }
    }
}
