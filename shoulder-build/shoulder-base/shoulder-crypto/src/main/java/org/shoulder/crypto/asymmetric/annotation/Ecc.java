package org.shoulder.crypto.asymmetric.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

/**
 * 标识注解。加在 AsymmetricCryptoProcessor 接口上，指定底层使用 ECC 加解密、签名验签
 *
 * @author lym
 */
@Qualifier
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Ecc {

}
