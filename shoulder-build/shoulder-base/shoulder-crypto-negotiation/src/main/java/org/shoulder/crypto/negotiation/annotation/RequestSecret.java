package org.shoulder.crypto.negotiation.annotation;

import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.*;

/**
 * 写在接口的参数 DTO 上，请求参数加密解密。
 * 请求发起端在请求前完成自动加密
 * 服务提供端在Controller方法中使用{@link RequestBody}来接收请求参数时，该注解写在参数类的字段上，在Spring mvc接收对象时的自动解密处理。
 *
 * @author lym
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@JacksonAnnotationsInside
//@JsonSerialize(using = RequestEncryptStdSerializer.class)
//@JsonDeserialize(using = RequestDecryptStdDeserializer.class)
public @interface RequestSecret {

}
