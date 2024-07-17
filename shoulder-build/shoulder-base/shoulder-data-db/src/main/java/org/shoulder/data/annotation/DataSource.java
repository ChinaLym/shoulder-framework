package org.shoulder.data.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义多数据源切换注解 (用于实现读写分离时不改代码)
 * 可以加在方法上，也可以加在类上，方法上优先
 *
 * @author lym
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DataSource {

    /**
     * 使用的数据源 bean 名称
     */
    String value();
}
