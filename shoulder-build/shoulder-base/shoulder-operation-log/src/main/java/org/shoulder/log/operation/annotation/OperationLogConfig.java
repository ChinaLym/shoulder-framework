package org.shoulder.log.operation.annotation;

import org.shoulder.log.operation.context.OperationContextStrategyEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * 该注解仅可以加在类上，为目标类的所有加 @OperationLog 的方法提供字段默认值，所有字段均为选填
 *
 * @author lym
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface OperationLogConfig {

    /**
     * 对象类型 例：角色： UserObjectTypes.ROLE
     * 【推荐实体实现 Operable，将不必在注解填充】
     */
    String objectType() default "";

    /**
     * 加了该注解的方法 A 中调用 加了该注解的方法 B 时，日志上下文创建策略
     * 默认，如果不存在嵌套调用，则新建一个上下文。若执行该方法时已经存在日志上下文，则不记录该方法的日志。
     */
    OperationContextStrategyEnum strategy() default OperationContextStrategyEnum.USE_DEFAULT;

}
