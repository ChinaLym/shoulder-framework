package org.shoulder.cluster.redis.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

/**
 * 应用专属，即使操作共享区域，也只是应用内部可见，如 A1、A2、A3、B1、B2、B3 6个应用，A1在 redis 放的 key，只能由 A1、A2、A3 可见，对B应用不可见
 * 原理 redisTemplate 自动添加应用标识 key 前缀。以区分不同应用的命名空间
 *
 * @author lym
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Qualifier
public @interface ApplicationExclusive {
}
