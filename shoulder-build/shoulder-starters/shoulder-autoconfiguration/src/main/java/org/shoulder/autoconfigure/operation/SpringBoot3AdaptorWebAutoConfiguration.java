package org.shoulder.autoconfigure.operation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 兼容  spring boot3 的操作：
 * <p>
 * http://itlym.cn/xxx/   http://itlym.cn/xxx 两个地址
 * spring boot 老版本：不区分都到一个 controller
 * spring boot 3.x新版本：controller 区分，可以到不同 controller，如果不实现则 404
 * <p>
 * 这个改动可能对用户体验影响较大，框架内默认兼容
 *
 * @author lym
 */
@ConditionalOnWebApplication
public class SpringBoot3AdaptorWebAutoConfiguration implements WebMvcConfigurer {

    // fixme spring boot 3.x 临时兼容url后缀/ 404 方案示例，长期要删除
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }

}
