package org.shoulder.log.operation;

import org.shoulder.log.operation.util.OpLogContext;

/**
 * shoulder 定义的业务，传播
 *
 * @author lym
 */
public enum OperationContextStrategyEnum implements OperationContextStrategy {

    // ------------------------------ 可以加在独立的业务方法上（没有肯定要新建） -------------------------------

    /**
     * 总是新建：没有则新建，有则暂时挂起父级业务 —— 记录的最全
     * （记录所有业务记录，包括触发上级业务时的子业务）
     * 场景：修改用户信息时，先将用户查出，再做更新。若更新时，也要记录查询业务，则查询这个业务就是必定新建。
     */
    NEW_OR_RENEW(CREATE_NEW, CREATE_NEW),

    /**
     * 有则放弃该业务，只保留外层业务记录 —— 记录最少，只记录触发点
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

    int onMissingContext;

    int onExistContext;

    OperationContextStrategyEnum(int onMissingContext, int onExistContext) {
        this.onMissingContext = onMissingContext;
        this.onExistContext = onExistContext;
    }

    // ----------------------- todo 实现这些策略 -----------------------------

    @Override
    public OpLogContext onMissingContext() {
        switch (onMissingContext) {
            case CREATE_NEW:
                break;
            case IGNORE:
                return null;
            case THROW_EX:
                break;
            default:
                throw new IllegalStateException("illegal strategy(value=" + onMissingContext + ")!");
        }
        return null;
    }


    @Override
    public OpLogContext onExistContext(OpLogContext existed) {
        switch (onExistContext) {
            case CREATE_NEW:
                break;
            case IGNORE:
                return null;
            case THROW_EX:
                break;
            case COPY:
                break;
            case REUSE:
                break;
            default:
                throw new IllegalStateException("illegal strategy(value=" + onExistContext + ")!");
        }
        return null;
    }
}
