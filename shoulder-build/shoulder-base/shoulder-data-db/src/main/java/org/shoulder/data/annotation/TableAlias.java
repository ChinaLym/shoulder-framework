package org.shoulder.data.annotation;

import java.lang.annotation.*;

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
