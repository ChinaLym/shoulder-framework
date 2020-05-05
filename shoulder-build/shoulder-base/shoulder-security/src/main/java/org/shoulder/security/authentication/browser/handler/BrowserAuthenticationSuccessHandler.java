package org.shoulder.security.authentication.browser.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 浏览器环境下认证成功的处理器
 * 继承了 spring 的默认处理器
 *todo
 *
 * @author lym
 */
public class BrowserAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private Logger log = LoggerFactory.getLogger(getClass());

    private String singInSuccessUrl;

    private RequestCache requestCache = new HttpSessionRequestCache();

    public BrowserAuthenticationSuccessHandler(String singInSuccessUrl) {
        this.singInSuccessUrl = singInSuccessUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.debug("authentication success.");
        super.onAuthenticationSuccess(request, response, authentication);

        //if (LoginResponseType.JSON.equals(browserProperties.getSignInResponseType())) {
        /*String type = authentication.getClass().getSimpleName();
        // 返回 msg:success
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(ResponseUtil.success());*/

		/*} else {
			// 如果设置了lym.security.browser.singInSuccessUrl，总是跳到设置的地址上
			if (StringUtils.isNotBlank(browserProperties.getSingInSuccessUrl())) {
				requestCache.removeRequest(request, response);
				setAlwaysUseDefaultTargetUrl(true);
				setDefaultTargetUrl(browserProperties.getSingInSuccessUrl());
			}
			// 如果没设置，则尝试跳转到登录之前访问的地址上，如果登录前访问地址为空，则跳到网站根路径上
			super.onAuthenticationSuccess(request, response, authentication);
		}*/
    }

}
