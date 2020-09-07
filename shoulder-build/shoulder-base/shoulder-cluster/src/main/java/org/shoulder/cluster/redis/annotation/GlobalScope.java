package org.shoulder.cluster.redis.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

/**
 * 所有服务共享。如 A1、A2、A3、B1、B2、B3 6个服务，A1服务放置于 redis 的 key，所有服务都可见
 *
 * @author lym
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Qualifier
@Deprecated
public @interface GlobalScope {
}
