package org.shoulder.crypto.negotiation.support;

import java.lang.annotation.*;

/**
 * 标识字段需要加密 或 标识类中含有加密的字段，Shoulder 会在在 请求发起方 或 服务提供方 接收请求时将自动为其加解密
 * <p>
 * 加在 DTO 的 String 类型字段上：
 * - encryptOnRequest  标识客户端请求时要加密，服务端收到请求时会解密
 * - decryptOnResponse 标识服务端返回响应时要加密，客户端端接收响应时要解密
 * <p>
 * 加在 DTO 的复杂类型字段上：
 * - 声明这个字段支持 DTO 嵌套，否则默认不处理复杂类内部字段
 * <p>
 * 加在 RestController 的方法上（接口）
 * - 为该接口实现加密增强：请求前判断是否为加密请求，不合规则拒绝，若是则自动解密参数
 * - 请求后自动重写返回值，加密响应
 * <p>
 * 加在 RestController 类上，标识该 Controller 中所有接口都需要加密增强，enhancer=false 的除外
 * <p>
 * todo 【语法糖】 添加 ElementType.PARAMETER 以支持 GET，同时需要实现 Spring MVC 参数解析器
 *
 * @author lym
 */
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sensitive {

    boolean enhancer() default true;

    /**
     * 请求时加密
     */
    boolean sensitiveRequest() default true;

    /**
     * 响应时加密
     */
    boolean sensitiveResponse() default true;

}
