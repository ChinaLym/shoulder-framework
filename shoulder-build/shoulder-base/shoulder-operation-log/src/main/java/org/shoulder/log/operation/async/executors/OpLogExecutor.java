package org.shoulder.log.operation.async.executors;

import org.shoulder.log.operation.async.OpLogRunnable;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

/**
 * 包装 Executor
 *
 * @author lym
 */
public class OpLogExecutor implements Executor {

    private final Executor delegate;

    public OpLogExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(@Nonnull Runnable command) {
        this.delegate.execute(new OpLogRunnable(command));
    }
}
