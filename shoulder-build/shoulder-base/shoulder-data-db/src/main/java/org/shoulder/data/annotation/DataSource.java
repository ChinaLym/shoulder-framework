package org.shoulder.data.annotation;

import org.shoulder.data.enums.DataSourceType;

import java.lang.annotation.*;

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
     * 切换数据源名称
     * 默认从写库进行操作（默认只有一个数据库，既写又读）
     */
    DataSourceType value() default DataSourceType.WRITE;
}