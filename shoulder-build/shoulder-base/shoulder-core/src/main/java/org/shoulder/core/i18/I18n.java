package org.shoulder.core.i18;

import org.shoulder.core.dto.response.BaseResult;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RestController 中加了该注解且返回值类型为 {@link BaseResult} 时 msg 字段将被自动翻译
 *
 * @author lym
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface I18n {

}
