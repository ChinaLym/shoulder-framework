package org.shoulder.validate.annotation;

import java.lang.annotation.*;

/**
 * 防止表单重复提交
 * @author lym
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RejectRepeatCommit {

}