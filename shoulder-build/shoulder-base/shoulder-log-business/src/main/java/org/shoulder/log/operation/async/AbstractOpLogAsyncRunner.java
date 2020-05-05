package org.shoulder.log.operation.async;

import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.Operator;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.util.OperationLogBuilder;
import org.shoulder.log.operation.util.OperationLogHolder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import java.util.List;

/**
 * 日志异步执行器公共方法
 *
 * @author lym
 */
public abstract class AbstractOpLogAsyncRunner {

    private static ThreadLocal<AbstractOpLogAsyncRunner> delegateMarkLocal = ThreadLocal.withInitial(() -> null);

    private static OperationLogger operationLogger;
    private OperationLogEntity opLogEntity;
    private List<? extends Operable> operableList;
    private Operator operator;

    /**
     * 暂存日志跨线程需要的信息
     * getLogWithoutCheck ： 可能原线程中没有，因此不应该检查
     * OperationLogUtils.autoLog 不需要跨线程，新的线程里采用默认值，标识在方法执行完毕时会记录日志
     */
    AbstractOpLogAsyncRunner() {
        this.opLogEntity = OperationLogHolder.getLogWithoutCheck();
        this.operableList = OperationLogHolder.getOperableObjects();
        this.operator = OperationLogBuilder.getDefaultOperator();
    }

    /**
     * 在运行前暂存所需信息
     */
    void before() {
        if (!hasEnhancer()) {
            markThis();
        }
        if(shouldEnhancer()){
            if (opLogEntity != null) {
                OperationLogHolder.setLog(this.opLogEntity.clone());
            }
            if (operableList != null) {
                OperationLogHolder.setOperableObjects(this.operableList);
            }
            if (operator != null) {
                OperationLogBuilder.setDefaultOperator(this.operator);
            }
            // 默认不自动记录
            OperationLogHolder.closeAutoLog();
        }


        // 释放自身的引用 help gc
        this.opLogEntity = null;
        this.operableList = null;
        this.operator = null;

    }

    /**
     * 在运行后删除保存的信息，需要在 finally 中调用
     */
    void after() {
        if (!shouldEnhancer()) {
            return;
        }
        // 记录日志
        if (operationLogger != null && OperationLogHolder.isEnableAutoLog()) {
            operationLogger.log();
        }

        // 清理线程变量
        OperationLogHolder.clean();
        OperationLogBuilder.clean();
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
    protected void markThis() {
        if (delegateMarkLocal.get() == null) {
            delegateMarkLocal.set(this);
        } else {
            // 重复包装，框架应该通过判断条件保证不会执行该代码
            throw new IllegalStateException("this runnable has enhancer more than once.");
        }
    }

    /**
     * 是否已经被增强
     */
    protected boolean hasEnhancer() {
        return delegateMarkLocal.get() != null;
    }


}
