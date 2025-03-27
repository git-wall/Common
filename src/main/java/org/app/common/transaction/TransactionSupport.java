package org.app.common.transaction;

import lombok.SneakyThrows;
import org.app.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

@Component
public class TransactionSupport {

    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED,
        rollbackFor = {BusinessException.class},
        timeout = 30
    )
    public <T> T execute(Supplier<T> action) {
        return action.get();
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED,
            rollbackFor = {BusinessException.class},
            timeout = 30
    )
    @SneakyThrows
    public <T> T execute(Callable<T> callable) {
        return callable.call();
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED,
        rollbackFor = {BusinessException.class},
        timeout = 30
    )
    public void execute(Runnable action) {
        action.run();
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRES_NEW,
        rollbackFor = {BusinessException.class},
        timeout = 30
    )
    public <T> T executeNewTransaction(Supplier<T> action) {
        return action.get();
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED,
        rollbackFor = {BusinessException.class},
        timeout = 30,
        readOnly = true
    )
    public <T> T executeReadOnly(Supplier<T> action) {
        return action.get();
    }
}