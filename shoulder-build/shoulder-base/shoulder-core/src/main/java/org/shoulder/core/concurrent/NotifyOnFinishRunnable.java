package org.shoulder.core.concurrent;

/**
 * NotifyOnFinishRunnable
 *
 * @author lym
 */
public class NotifyOnFinishRunnable implements Runnable {

    private final Runnable delegate;

    private final Callback callback;

    public NotifyOnFinishRunnable(Runnable delegate, Callback callback) {
        this.delegate = delegate;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            delegate.run();
        } finally {
            callback.callBack();
        }
    }

    @FunctionalInterface
    public interface Callback {

        void callBack();

    }
}
