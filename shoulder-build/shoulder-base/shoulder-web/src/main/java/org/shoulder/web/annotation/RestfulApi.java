package org.shoulder.web.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * 统一返回值包装
 * 在 RestController 基础上，该类所有方法跳过标准返回值包装
 * 加方法上，返回值统一包装
 * code、msg、data，其中接口返回值作为 data
 *
 * @author lym
 */
@RestController
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestfulApi {
    @AliasFor(
        annotation = RestController.class
    )
    String value() default "";
}
