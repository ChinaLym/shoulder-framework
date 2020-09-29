package org.shoulder.log.operation.async;

/**
 * 继承日志相关线程变量的 Runnable
 *
 * @author lym
 */
public class OpLogRunnable extends AbstractOpLogAsyncRunner implements Runnable {

    private final Runnable delegate;

    public OpLogRunnable(Runnable delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public void run() {
        try {
            // 1. set 进对应持有者
            before();

            // 2. 执行任务
            this.delegate.run();

        } catch (Exception e) {
            error();
            throw e;
        } finally {

            // 3. 清理持有者的变量信息
            after();
        }
    }

}
