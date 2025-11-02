package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.application.out.LockOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * User-Level Lock
 */
@RequiredArgsConstructor
@Repository
public class NamedLockRepository implements LockOutPort {

    private final JdbcTemplate jdbcTemplate;

    /***
     * lock 획득
     * @param lockName
     * @param timeoutSeconds
     * @return 1:성공 0:타임아웃 null: 오류
     */
    @Override
    public Integer getLock(String lockName, int timeoutSeconds) {
        return jdbcTemplate.queryForObject(
                "SELECT GET_LOCK(?, ?)",
                Integer.class,
                lockName,
                timeoutSeconds
        );
    }

    /**
     * lock 해제
     * @param lockName
     * @return 1: 성공, 0: 미존재 lock, null: 오류
     */
    @Override
    public Integer releaseLock(String lockName) {
        return jdbcTemplate.queryForObject(
                "SELECT RELEASE_LOCK(?)",
                Integer.class,
                lockName
        );
    }
}
