package org.shoulder.web.annotation;

import java.lang.annotation.*;

/**
 * 跳过统一返回值包装
 * 加在 RestController 类上，该类所有方法跳过标准返回值包装
 * 加方法上，目标方法跳过包装
 *
 * @author lym
 */
@Inherited
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipResponseWrap {

}