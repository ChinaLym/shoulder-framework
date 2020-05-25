package org.shoulder.log.operation.async;

import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.util.OpLogContext;
import org.shoulder.log.operation.util.OpLogContextHolder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

/**
 * 日志异步执行器公共方法
 *
 * @author lym
 */
public abstract class AbstractOpLogAsyncRunner {

    /**
     * 避免重复包装引入的问题
     */
    private ThreadLocal<AbstractOpLogAsyncRunner> delegateMarkLocal = ThreadLocal.withInitial(() -> null);

    /**
     *
     */
    private static OperationLogger operationLogger;

    private OpLogContext opLogContext;



    /**
     * 暂存日志跨线程需要的信息
     * getLogWithoutCheck ： 可能原线程中没有，因此不应该检查
     * OperationLogUtils.autoLog 不需要跨线程，新的线程里采用默认值，标识在方法执行完毕时会记录日志
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
            if (this.opLogContext.getLogEntity() != null) {
                OpLogContextHolder.setLog(this.opLogContext.getLogEntity().clone());
            }
            // 默认不自动记录
            OpLogContextHolder.closeAutoLog();
        }

        // 释放自身的引用 help gc
        this.opLogContext = null;
        OpLogContextHolder.clean();
    }

    /**
     * 在运行后删除保存的信息，需要在 finally 中调用
     */
    void after() {
        if (!shouldEnhancer()) {
            return;
        }
        // 记录日志
        if (operationLogger != null && OpLogContextHolder.isEnableAutoLog()) {
            operationLogger.log();
        }

        // 清理线程变量
        OpLogContextHolder.clean();
        delegateMarkLocal.remove();
    }

    public static void setOperationLogger(OperationLogger operationLogger) throws BeansException {
        AbstractOpLogAsyncRunner.operationLogger = operationLogger;
        if (operationLogger == null) {
            LoggerFactory.getLogger(AbstractOpLogAsyncRunner.class).warn("async warp logger is null!");
        } else {
            LoggerFactory.getLogger(AbstractOpLogAsyncRunner.class).info("async warp logger:" + operationLogger.getClass().getSimpleName());

        }

    }

    protected boolean shouldEnhancer() {
        if (delegateMarkLocal.get() == null) {
            throw new IllegalStateException("must has been enhancer");
        }
        return delegateMarkLocal.get() == this;
    }

    /**
     * 将增强者设为自己
     */
    protected synchronized void markThis() {
        if (delegateMarkLocal.get() == null) {
            delegateMarkLocal.set(this);
        } else {
            // 重复包装，框架应该通过判断条件保证不会执行该代码
            throw new IllegalStateException("this runnable has enhancer more than once.");
        }
    }

    /**
     * 未被增强
     */
    protected boolean notEnhancer() {
        return delegateMarkLocal.get() == null;
    }


}
