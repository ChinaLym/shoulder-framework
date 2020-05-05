package org.shoulder.cluster.redis.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

/**
 * 服务专属，即使操作共享区域，也只是服务内部可见，如 A1、A2、A3、B1、B2、B3 6个服务，A1服务放置于 redis 的 key，只能由 A1、A2、A3 可见，对B服务不可见
 * 原理 redisTemplate 自动添加服务名 key 前缀。以区分不同服务的命名空间
 * @author lym
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Qualifier
public @interface ServiceExclusive {
}
