package org.shoulder.security.authentication.browser.handler;

import org.shoulder.core.util.StringUtils;
import org.shoulder.security.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 浏览器环境下认证成功的处理器
 * 继承了 spring 的默认处理器（登录成功后跳跳转到登录之前访问的地址上，如果登录前访问地址为空，则跳到网站根路径上）
 * 在其基础上新增返回 json 类型
 * 实际应用中如果由更复杂的判断逻辑，可继承该类或实现个性的 AuthenticationSuccessHandler 并注入
 *
 * @author lym
 */
public class BrowserAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ResponseType responseType;

    public BrowserAuthenticationSuccessHandler(ResponseType responseType, String singInSuccessUrl) {
        this.responseType = responseType;
        if(ResponseType.REDIRECT == responseType){
            if (StringUtils.isNotBlank(singInSuccessUrl)) {
                // 如果设置了 shoulder.security.browser.singInSuccessUrl，总是跳到设置的地址上
                setAlwaysUseDefaultTargetUrl(true);
                setDefaultTargetUrl(singInSuccessUrl);
            }
            // 如果没设置，则尝试跳转到登录之前访问的地址上，如果登录前访问地址为空，则跳到网站根路径上
        }
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.debug("authentication SUCCESS.");

        if (ResponseType.JSON.equals(responseType)) {
            log.debug("json response for {}", request.getRequestURI());
            // 为了支持旧版本的浏览器
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.getWriter().write(ResponseUtil.success());
		} else if(ResponseType.REDIRECT.equals(responseType)){
            log.debug("redirect for {}", request.getRequestURI());
			super.onAuthenticationSuccess(request, response, authentication);
		} else {
            throw new IllegalStateException("");
        }
    }


}
