package org.shoulder.security.authentication.handler.url;

import cn.hutool.core.util.StrUtil;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

/**
 * 浏览器退出登录成功默认逻辑
 * 如果设置了 signOutSuccessUrl 则跳转至退出登录页面，否则跳转至主页
 *
 * @author lym
 * @see SimpleUrlLogoutSuccessHandler
 */
public class RedirectLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

    /**
     * 构造
     *
     * @param signOutSuccessUrl 退出成功后跳转到哪，固定值
     */
    public RedirectLogoutSuccessHandler(String signOutSuccessUrl) {
        if (StrUtil.isNotBlank(signOutSuccessUrl)) {
            setAlwaysUseDefaultTargetUrl(true);
            setDefaultTargetUrl(signOutSuccessUrl);
        }
    }

}
