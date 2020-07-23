package org.shoulder.log.operation.async;

import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.util.OpLogContext;
import org.shoulder.log.operation.util.OpLogContextHolder;

/**
 * 日志异步执行器公共方法
 *
 * @author lym
 */
public abstract class AbstractOpLogAsyncRunner {

    /**
     * 实际增强者
     * 避免重复包装引入的问题
     */
    private ThreadLocal<AbstractOpLogAsyncRunner> enhancerLocal = ThreadLocal.withInitial(() -> null);

    private OpLogContext opLogContext;

    /**
     * 暂存日志跨线程需要的信息
     * getLogWithoutCheck ： 可能原线程中没有，因此不应该检查
     */
    AbstractOpLogAsyncRunner() {
        this.opLogContext = OpLogContextHolder.getContext();
    }

    /**
     * 在运行前暂存所需信息
     */
    void before() {
        if (notEnhancer()) {
            markThis();
        }
        if(shouldEnhancer()){
            if (this.opLogContext != null) {
                OpLogContextHolder.setLog(this.opLogContext.getOperationLog().cloneTo(new OperationLogDTO()));
            }
        }

        // 释放自身的引用 help gc
        this.opLogContext = null;
    }

    /**
     * 在运行后删除保存的信息，需要在 finally 中调用
     */
    void after() {
        if (!shouldEnhancer()) {
            return;
        }
        // 清理线程变量
        OpLogContextHolder.clean();
        enhancerLocal.remove();
    }

    /**
     * 是否需要增强（本线程指定的增强者是否为自己）
     */
    protected boolean shouldEnhancer() {
        if (enhancerLocal.get() == null) {
            throw new IllegalStateException("must has been enhancer");
        }
        return enhancerLocal.get() == this;
    }

    /**
     * 将线程的增强者设为自己
     */
    protected synchronized void markThis() {
        if (enhancerLocal.get() == null) {
            enhancerLocal.set(this);
        } else {
            // 重复包装，框架保证不会执行该代码
            throw new IllegalStateException("this runnable has enhancer more than once.");
        }
    }

    /**
     * 未被任何线程增强
     */
    protected boolean notEnhancer() {
        return enhancerLocal.get() == null;
    }


}
