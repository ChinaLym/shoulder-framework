package org.shoulder.security.authentication.browser.handler;

import cn.hutool.core.util.StrUtil;
import org.shoulder.security.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 浏览器退出登录成功默认逻辑
 * 如果设置了 signOutSuccessUrl 则跳转至退出登录页面，否则跳转至主页，SimpleUrlLogoutSuccessHandler
 * 浏览器清理token
 *
 * @author lym
 */
public class BrowserLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

    private final ResponseType responseType;

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 构造
     *
     * @param responseType      响应类型
     * @param signOutSuccessUrl 退出成功后跳转到哪
     */
    public BrowserLogoutSuccessHandler(ResponseType responseType, String signOutSuccessUrl) {
        this.responseType = responseType;
        if (StrUtil.isNotBlank(signOutSuccessUrl)) {
            setAlwaysUseDefaultTargetUrl(true);
            setDefaultTargetUrl(signOutSuccessUrl);
        }
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {

        log.debug("logout SUCCESS.");

        if (ResponseType.JSON.equals(responseType)) {
            log.debug("json response for {}", request.getRequestURI());
            // 为了支持旧版本的浏览器
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.getWriter().write(ResponseUtil.success());
        } else if (ResponseType.REDIRECT.equals(responseType)) {
            log.debug("redirect for {}", request.getRequestURI());
            super.onLogoutSuccess(request, response, authentication);
        } else {
            throw new IllegalStateException("");
        }
    }

}
