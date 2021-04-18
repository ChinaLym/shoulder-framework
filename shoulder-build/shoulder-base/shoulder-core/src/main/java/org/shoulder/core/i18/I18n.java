package org.shoulder.core.i18;

import org.shoulder.core.dto.response.BaseResult;

import java.lang.annotation.*;

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
