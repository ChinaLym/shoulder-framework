package org.shoulder.security.authentication.handler;

import org.shoulder.core.util.StringUtils;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * 浏览器环境下认证成功的处理器
 * 继承了 spring 的默认处理器（登录成功后跳跳转到登录之前访问的地址上，如果登录前访问地址为空，则跳到网站根路径上）
 *
 * @author lym
 */
public class RedirectAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public RedirectAuthenticationSuccessHandler(String singInSuccessUrl) {
        if (StringUtils.isNotBlank(singInSuccessUrl)) {
            // 如果设置了 shoulder.security.browser.singInSuccessUrl，总是跳到设置的地址上
            setAlwaysUseDefaultTargetUrl(true);
            setDefaultTargetUrl(singInSuccessUrl);
        }
        // 如果没设置，则尝试跳转到登录之前访问的地址上，如果登录前访问地址为空，则跳到网站根路径上
    }

}
