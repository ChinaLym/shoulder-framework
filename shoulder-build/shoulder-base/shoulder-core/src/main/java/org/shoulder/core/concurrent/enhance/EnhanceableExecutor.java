package org.shoulder.core.concurrent.enhance;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

/**
 * 包装 Executor
 *
 * @author lym
 */
public class EnhanceableExecutor implements Executor {

    private final Executor delegate;

    public EnhanceableExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(@Nonnull Runnable command) {
        this.delegate.execute(ThreadEnhanceHelper.doEnhance(command));
    }
}
