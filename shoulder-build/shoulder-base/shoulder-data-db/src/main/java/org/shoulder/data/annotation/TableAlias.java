package org.shoulder.data.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * table别名 (希望即使数据库表名修改，也不会干扰代码的 mapper.xml)
 *
 * @author lym
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface TableAlias {

    /**
     * 别名
     */
    String value();
}
