package org.shoulder.log.operation.context;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.model.Operable;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.model.OperationLogDTO;
import org.shoulder.log.operation.model.Operator;
import org.springframework.beans.BeansException;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 该类充当 OperationLogHolder 的角色。set日志对象后，可以通过该工具在同一线程内操作这个日志对象。
 * 主要为了配合 {@link OperationLog}注解的使用
 * 若应用希望扩展该类能力：允许以继承或组合方式调用类，并使用新的类，增加扩展能力。
 * todo 【优化-扩展性】获取上下文，若不存在时，默认策略异常 → 忽略？或者可配置？
 *
 * @author lym
 */
public class OpLogContextHolder {

    private final static Logger log = ShoulderLoggers.SHOULDER_DEFAULT;

    /**
     * 保存操作日志上下文
     */
    private static final ThreadLocal<OpLogContext> CURRENT_OP_LOG_CONTEXT = new ThreadLocal<>();

    /**
     * 操作日志记录器
     */
    private static OperationLogger operationLogger;

    /**
     * 记录日志
     */
    public static void log() {
        OpLogContext context = CURRENT_OP_LOG_CONTEXT.get();
        if (context != null) {
            OperationLogDTO opLog = OpLogContextHolder.getLog();
            List<? extends Operable> operableCollection = OpLogContextHolder.getOperableObjects();
            if (opLog.getEndTime() == null) {
                opLog.setEndTime(Instant.now());
            }
            if (CollectionUtils.isEmpty(operableCollection)) {
                operationLogger.log(opLog);
            } else {
                operationLogger.log(opLog, operableCollection);
            }
        }
    }

    // ============================ 日志上下文 =======================

    /**
     * 获取当前上下文，并检查状态
     */
    @Nonnull
    public static OpLogContext getContextOrException() {
        OpLogContext context = CURRENT_OP_LOG_CONTEXT.get();
        if (context == null) {
            throw new IllegalThreadStateException(
                // Helpful Tip: Add a breakpoint here and debug your code
                "No OpLogContext in concurrentThread! Maybe your method miss @OperationLog " +
                    "or used [SpringAop] incorrectly! Thread - [" + Thread.currentThread().getName() + "]. ");
        }
        return context;
    }

    /**
     * 不经过检查，直接获取日志上下文
     */
    @Nullable
    public static OpLogContext getContext() {
        return CURRENT_OP_LOG_CONTEXT.get();
    }

    /**
     * 设置日志上下文
     */
    public static void setContext(OpLogContext opLogContext) {
        CURRENT_OP_LOG_CONTEXT.set(opLogContext);
    }

    // ============================ 日志实体 =======================

    /**
     * 获取日志实体
     *
     * @return 日志上下文中日志实体
     */
    public static OperationLogDTO getLog() {
        return getContextOrException().getOperationLog();
    }

    /**
     * 设置日志实体
     */
    public static void setLog(OperationLogDTO entity) {
        getContextOrException().setOperationLog(entity);
    }

    // ============================ 当前用户） =======================

    /**
     * @return 日志上下文中 当前用户
     */
    public static Operator getOperator() {
        return getContextOrException().getOperator();
    }

    /**
     * 设置用户信息
     */
    public static void setOperator(Operator entity) {
        getContextOrException().setOperator(entity);
    }

    // ============================ 批量被操作对象（用于一个方法记录多条操作日志） =======================


    /**
     * 批量设置被操作对象
     */
    public static void setOperableObject(Operable operableObj) {
        getContextOrException().getOperationLog().setOperableObject(operableObj);
    }


    /**
     * 获取日志上下文中所有被操作对象
     */
    public static List<Operable> getOperableObjects() {
        return getContextOrException().getOperableObjects();
    }

    /**
     * 批量设置被操作对象
     */
    public static void setOperableObjects(Collection<? extends Operable> operableObjs) {
        getContextOrException().setOperableObjects(new ArrayList<>(operableObjs));
    }

    /**
     * 添加多个被操作对象
     */
    public static void addOperableObjects(Collection<? extends Operable> operableObjs) {
        if (operableObjs == null) {
            return;
        }
        List<Operable> local = getOperableObjects();
        if (local != null) {
            local.addAll(operableObjs);
        } else {
            setOperableObjects(new ArrayList<>(operableObjs));
        }
    }


    // ============================ 记录日志开关 =========================

    /**
     * 是否开启自动记录日志
     */
    public static boolean isEnableAutoLog() {
        // 默认开启
        return getContextOrException().isAutoLog();
    }

    /**
     * 临时关闭自动记录日志，一般用于表示希望 @OperationLog 只生成操作日志对象，而不记录。
     * <p>
     * 场景：批量导入时，使用新线程对导入内容的校验，日志记录也在新日志上下文中做， 处理请求的方法中不希望记录操作日志
     * 在方法中调用 closeAutoLog，告诉框架在本方法结束后不要记录操作日志
     */
    public static void closeAutoLog() {
        getContextOrException().setAutoLog(false);
    }

    /**
     * 重新开启自动记录日志
     */
    public static void enableAutoLog() {
        getContextOrException().setAutoLog(true);
    }


    /**
     * 抛异常后是否记录操作日志
     */
    public static boolean isLogWhenThrow() {
        // 默认开启
        return getContextOrException().isLogWhenThrow();
    }

    /**
     * 抛异常后是否记录操作日志
     */
    public static void setLogWhenThrow(boolean logWhenThrow) {
        getContextOrException().setLogWhenThrow(logWhenThrow);
    }

    /**
     * 线程变量清理：
     * 除非你很明白框架原理，否则不要主动调用该方法
     */
    public static void clean() {
        CURRENT_OP_LOG_CONTEXT.remove();
    }

    public static void setOperationLogger(OperationLogger opLogger) throws BeansException {
        operationLogger = opLogger;
        if (opLogger == null) {
            log.warn("operationLogger is null!");
        } else {
            log.info("operationLogger:" + opLogger.getClass().getSimpleName());

        }
    }
}
