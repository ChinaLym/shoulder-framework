package org.shoulder.log.operation.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *  该注解仅可以加在类上，为目标类的所有加 @OperationLog 的方法提供字段默认值，所有字段均为选填
 *
 * @author lym
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLogConfig {

    /**
     * 对象类型 例：监控点： VmsObjectTypes.CAMERA
     * 【推荐实体实现 Operable，将不必在注解填充】
     */
    String objectType() default "";

}
