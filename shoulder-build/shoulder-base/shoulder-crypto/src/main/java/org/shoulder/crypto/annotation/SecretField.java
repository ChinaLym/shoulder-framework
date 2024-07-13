package org.shoulder.crypto.annotation;

import java.lang.annotation.*;

/**
 * 私密字段，表示需要加密或者解密
 *
 * @author lym
 * @deprecated 暂无任何效果，开发中
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SecretField {
}
