package org.shoulder.security.authentication.handler.url;

import org.shoulder.core.util.StringUtils;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 * 浏览器环境下认证失败的默认处理器
 * 继承了 spring 的默认处理器，除了跳转url外
 *
 * @author lym
 */
public class RedirectAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public RedirectAuthenticationFailureHandler(String authFailUrl) {
        if (StringUtils.isNotBlank(authFailUrl)) {
            super.setDefaultFailureUrl(authFailUrl);
            // 使用请求转发替代重定向，减少请求次数，适合非分布式场景
            super.setUseForward(true);
        }
    }
}
