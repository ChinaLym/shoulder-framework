package org.shoulder.crypto.negotiation.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.shoulder.crypto.negotiation.annotation.support.ResponseDecryptStdDeserializer;

import java.lang.annotation.*;

/**
 * 标识字段为秘密字段，在 请求发起方 或 服务提供方 接收请求时将自动为其加解密
 * 综合了 {@link RequestSecret}、{@link ResponseSecret} 两个注解
 *
 * @author lym
 * @deprecated 暂时不做，建议还是采用另外两个注解，以保证更高的可读性
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonDeserialize(using = ResponseDecryptStdDeserializer.class)
public @interface Secret {

}
