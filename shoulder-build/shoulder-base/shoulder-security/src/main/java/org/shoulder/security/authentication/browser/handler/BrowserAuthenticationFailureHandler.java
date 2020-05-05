package org.shoulder.security.authentication.browser.handler;

import org.shoulder.security.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 浏览器环境下认证失败的默认处理器
 * 继承了 spring 的默认处理器
 *
 * @author lym
 */
public class BrowserAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.info("登录失败");

        //if (LoginResponseType.JSON.equals(securityProperties.getBrowser().getSignInResponseType())) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(ResponseUtil.jsonMsg(exception.getMessage()));
		/*}else{
			super.onAuthenticationFailure(request, response, exception);
		}*/

    }
}
