package org.shoulder.log.operation.context;

import jakarta.annotation.Nonnull;

/**
 * shoulder 定义的业务，传播。
 * 变量命名：A_OR_B (if exist then A else then B)
 *
 * 推荐使用 NEW_OR_IGNORE、ALWAYS_NEW
 *
 * @see OperationContextStrategy 策略介绍
 * @author lym
 */
public enum OperationContextStrategyEnum implements OperationContextStrategy {

    // ------------------------------ 可以加在独立的业务方法上（没有肯定要新建） -------------------------------

    /**
     * 取 DEFAULT 行为：不会直接应用，用户可以通过调用 {@link #setDefault} 方法改变默认业务传播行为
     * @see #DEFAULT
     */
    USE_DEFAULT(-1, -1),

    /**
     * 总是新建：没有则新建，有则暂时挂起父级业务并新建 —— 记录的最全
     * （记录所有业务记录，包括触发上级业务时的子业务）
     * 场景：修改用户信息时，先将用户查出，再做更新。若更新时，也要记录查询业务，则查询这个业务就是必定新建。
     */
    ALWAYS_NEW(CREATE_NEW, CREATE_NEW),

    /**
     * 有则忽略：只保留外层业务记录 —— 记录最少，只记录触发点
     * （只记录外层业务记录，不记录子业务）
     * 场景：修改用户信息时，先将用户查出，再做更新。若更新时，不记录查询业务，则查询这个业务就是有也不使用。
     */
    NEW_OR_IGNORE(CREATE_NEW, IGNORE),


    // ------------------------------ 加在不可作为独立业务的方法上（必须已经有） -------------------------------

    /**
     * 不需要：如果有则使用，没有不报错【一般不推荐已存在则复用，容易导致污染上级业务记录】
     * （说明这是一个业务子流程方法，且该方法只能由加了业务日志注解的方法调用）
     * 场景：下单业务，扣钱、扣库存、创建订单中，扣库存的方法中选择性记录部分操作详情，且这个扣库存可以单独使用，那扣库存的流程所在方法就可以是一个
     */
    IGNORE_OR_REUSE(IGNORE, REUSE),

    /**
     * 必须要有：有则使用，没有则报错
     * 说明这是一个业务子流程方法，且该方法只能由加了业务日志注解的方法调用。
     * 场景：下单业务，扣钱、扣库存、创建订单中，扣库存的方法中可能需要记录部分操作详情，且这个扣库存不可单独使用，那扣库存的流程所在方法就可以是一个
     */
    EX_OR_REUSE(THROW_EX, REUSE),

    ;

    /**
     * 默认行为
     */
    public static OperationContextStrategy DEFAULT = NEW_OR_IGNORE;

    int onMissingContext;

    int onExistContext;

    OperationContextStrategyEnum(int onMissingContext, int onExistContext) {
        this.onMissingContext = onMissingContext;
        this.onExistContext = onExistContext;
    }

    public static OperationContextStrategy getDefault() {
        return DEFAULT;
    }

    public static void setDefault(OperationContextStrategy defaultStrategy) {
        DEFAULT = defaultStrategy;
    }

    // ----------------------- 策略 -----------------------------

    @Nonnull
    @Override
    public OpLogContext onMissingContext() {
        return switch (onMissingContext) {
            case CREATE_NEW -> OpLogContext.builder()
                    .currentOperator(OpLogContext.getCurrentOperator())
                    .build();
            case IGNORE -> OpLogContext.builder()
                    .currentOperator(OpLogContext.getCurrentOperator())
                    .autoLog(false)
                    .logWhenThrow(false)
                    .build();
            // 必须要有上下文，编码者期望调用该方法时线程中有上下文对象，结果却没有
            case THROW_EX -> throw new IllegalStateException(
                    "can't invoke without an opLogContext in thread with such onMissingContext strategy!");
            // 禁止使用非法值，其他值对于添加无意义
            default -> throw new IllegalStateException("illegal strategy(value=" + onMissingContext + ")!");
        };
    }


    @Nonnull
    @Override
    public OpLogContext onExistContext(OpLogContext existed) {
        return switch (onExistContext) {
            case CREATE_NEW -> OpLogContext.builder()
                    .currentOperator(OpLogContext.getCurrentOperator())
                    .build();
            case IGNORE -> OpLogContext.builder()
                    .currentOperator(OpLogContext.getCurrentOperator())
                    .autoLog(false)
                    .logWhenThrow(false)
                    .build();
            // 必须没有上下文（不能被外部调用），编码者期望调用该方法时线程中没有上下文对象，结果却已经存在
            case THROW_EX -> throw new IllegalStateException("can't invoke with already had an opLogContext in thread!");
            case COPY -> OpLogContext.copy(existed)
                    .setParent(existed);
            case REUSE -> existed;
            default -> throw new IllegalStateException("illegal strategy(value=" + onExistContext + ")!");
        };
    }
}
