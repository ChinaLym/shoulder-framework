package org.shoulder.data.annotation;

import java.lang.annotation.*;

/**
 * bizId 源字段
 *
 * @author lym
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface BizIdSource {

}
