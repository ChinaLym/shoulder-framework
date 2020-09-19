package org.shoulder.crypto.negotiation.annotation;

import java.lang.annotation.*;

/**
 * 标识字段需要加密 或 标识类中含有加密的字段，Shoulder 会在在 请求发起方 或 服务提供方 接收请求时将自动为其加解密
 *
 * @author lym
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sensitive {

    /**
     * 请求时加密
     */
    boolean requestSensitive() default true;

    /**
     * 响应时加密
     */
    boolean responseSensitive() default true;

}
