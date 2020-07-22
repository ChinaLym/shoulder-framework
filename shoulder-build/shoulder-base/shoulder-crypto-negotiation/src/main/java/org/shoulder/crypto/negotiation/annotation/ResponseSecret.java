package org.shoulder.crypto.negotiation.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.shoulder.crypto.negotiation.annotation.support.ResponseDecryptStdDeserializer;
import org.shoulder.crypto.negotiation.annotation.support.ResponseEncryptStdSerializer;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;

/**
 * 请求响应 DTO 自动加解密
 * 服务提供端controller方法中使用{@link ResponseBody}来返回json数据时，该注解写在指定需要加密返回的对象字段上，在Spring mvc返回json数据的加密处理。
 * 消费端请求完毕后自动解密对应字段
 *
 * @author lym
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = ResponseEncryptStdSerializer.class)
@JsonDeserialize(using = ResponseDecryptStdDeserializer.class)
public @interface ResponseSecret {

}
