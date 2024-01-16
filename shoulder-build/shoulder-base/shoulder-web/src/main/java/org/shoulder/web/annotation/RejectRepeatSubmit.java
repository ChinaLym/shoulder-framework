package org.shoulder.web.annotation;

import java.lang.annotation.*;

/**
 * 拒绝表单重复提交
 * 用法：加在 Controller 的方法上，提交请求时会校验 token
 * 注意：防抖尽量在前端做！
 *
 * @author lym
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RejectRepeatSubmit {

}
