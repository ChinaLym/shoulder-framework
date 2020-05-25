package org.shoulder.log.operation.async;

import org.shoulder.log.operation.util.OpLogContextHolder;

import java.util.concurrent.Callable;

/**
 * 继承日志相关线程变量的 callable
 * @author lym
 */
public class OpLogCallable<V> extends AbstractOpLogAsyncRunner implements Callable<V> {


    private final Callable<V> delegate;

    public OpLogCallable(Callable<V> delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public V call() throws Exception {
        try {
            // 1. set 进对应持有者
            before();

            // 2. 执行任务
            return this.delegate.call();

        }catch (Exception e){
            // 如果异常则记录失败
            OpLogContextHolder.getLog().setResultFail();
            throw e;
        }finally {
           // 3. 清理持有者的变量信息
            after();
        }
    }


}
