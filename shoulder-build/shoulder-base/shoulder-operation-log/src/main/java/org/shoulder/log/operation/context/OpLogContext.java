package org.shoulder.log.operation.context;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.dto.Operator;
import org.shoulder.log.operation.dto.SystemOperator;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 操作日志上下文
 * 这些参数决定本次操作日志如何记录
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
public class OpLogContext {

    /**
     * 保存当前线程用户信息
     */
    private static ThreadLocal<Operator> currentOperatorThreadLocal = new ThreadLocal<>();

    public static void setDefaultOperator(Operator operator) {
        currentOperatorThreadLocal.set(operator);
    }

    public static Operator getCurrentOperator() {
        Operator operator = currentOperatorThreadLocal.get();
        return operator == null ? SystemOperator.getInstance() : operator;
    }

    /**
     * 清理线程变量
     * 推荐由拦截器负责，使用者不要在业务代码中调用该方法，除非你很清楚日志框架的原理
     */
    public static void cleanDefaultOperator() {
        currentOperatorThreadLocal.remove();
    }

    // -----------------------------------------------------------------------------------------

    /**
     * 上級上下文
     */
    private OpLogContext parent = null;

    /**
     * 操作日志對象
     */
    private OperationLogDTO operationLog;

    /**
     * 当被操作对象为多个时，保存在这里
     */
    private List<Operable> operableObjects;

    /**
     * 触发本业务的操作者信息
     */
    private Operator operator;

    /**
     * 是否在方法正常结束后自动记录日志
     */
    private boolean autoLog = true;

    /**
     * 注解所在方法抛异常后是否继续记录日志
     */
    private boolean logWhenThrow = true;

    OpLogContext() {
        Operator operator = currentOperatorThreadLocal.get();
        setOperator(operator != null ? operator : SystemOperator.getInstance());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static OpLogContext copy(OpLogContext lastOpLogContext) {
        if (lastOpLogContext != null) {
            return new OpLogContext()
                .setOperator(lastOpLogContext.getOperator())
                .setAutoLog(lastOpLogContext.isAutoLog())
                .setLogWhenThrow(lastOpLogContext.isLogWhenThrow());
        } else {
            return new OpLogContext();
        }
    }

    public void setOperableObjects(Collection<? extends Operable> operableObjs) {
        if (operableObjs != null) {
            this.operableObjects = new ArrayList<>(operableObjs);
        }
    }

    public static final class Builder {

        private final OpLogContext opLogContext;

        private Builder() {
            opLogContext = new OpLogContext();
        }

        public static Builder anOpLogContext() {
            return new Builder();
        }

        public Builder parent(OpLogContext parent) {
            opLogContext.setParent(parent);
            return this;
        }

        public Builder operationLog(OperationLogDTO operationLog) {
            opLogContext.setOperationLog(operationLog);
            return this;
        }

        public Builder operableObjects(Collection<? extends Operable> operableObjects) {
            opLogContext.setOperableObjects(operableObjects);
            return this;
        }

        public Builder currentOperator(Operator currentOperator) {
            opLogContext.setOperator(currentOperator);
            return this;
        }

        public Builder autoLog(boolean autoLog) {
            opLogContext.setAutoLog(autoLog);
            return this;
        }

        public Builder logWhenThrow(boolean logWhenThrow) {
            opLogContext.setLogWhenThrow(logWhenThrow);
            return this;
        }

        public OpLogContext build() {
            Assert.notNull(opLogContext.getOperator(), "currentOperator can't be null!");
            return opLogContext;
        }
    }
}
